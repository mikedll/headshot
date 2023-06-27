package com.mikedll.headshot;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.io.File;
import java.io.IOException;

import io.github.cdimascio.dotenv.Dotenv;
import org.javatuples.Pair;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.Migrations;
import com.mikedll.headshot.controller.Scanner;
import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.controller.RequestHandler;
import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.apiclients.ApiClientManager;
import com.mikedll.headshot.experiments.Experiment;
import com.mikedll.headshot.model.Formats;

public class Application {

    public static Application current;
    
    public DatabaseConfiguration dbConf;

    public ApiClientManager apiClientManager;

    public List<RequestHandler> requestHandlers;

    public AssetFingerprinter assetFingerprinter = new AssetFingerprinter();

    public Config config;

    public TemplateEngine templateEngine;

    public Logger logger;
    
    public void setConfig(Config config) {
        this.config = config;
    }
    
    public void run(String[] args) {
        loadConfig();
        loggingSetup();
        dbSetup();

        if(argInterception(args)) {
            return;
        }

        String error = webSetup();
        if(error != null) {
            System.out.println(error);
            shutdown();
            return;
        }

        runTomcat();
        // runExp1();

        shutdown();
    }

    public void loggingSetup() {
        ConfigurationFactory.setConfigurationFactory(new LoggingConfigFactory(config));
        this.logger = LogManager.getLogger("com.mikedll.headshot.Application");
        this.logger.info("Starting app in " + this.config.env + " environment");
    }

    public void dbSetup() {
        this.dbConf = new DatabaseConfiguration(this.config);
    }

    public boolean argInterception(String[] args) {
        if(args.length == 1 && args[0].equals("migrate")) {
            Migrations migrations = new Migrations(this.dbConf);
            String error = migrations.readMigrations();
            if(error != null) {
                this.logger.error("Error: " + error);
                shutdown();
                return true;
            }
            error = migrations.migrateForward();
            if(error != null) {
                this.logger.error("Error: " + error);
            }
            shutdown();
            return true;
        } else if(args.length == 2 && args[0].equals("migrate:reverse")) {
            Migrations migrations = new Migrations(this.dbConf);
            String error = migrations.readMigrations();
            if(error != null) {
                this.logger.error("Error: " + error);
                shutdown();
                return true;
            }
            error = migrations.reverse(args[1]);
            if(error != null) {
                this.logger.error("Error: " + error);
            }
            shutdown();
            return true;
        } else if(args.length == 2 && args[0].equals("gen_migration")) {
            String filename = String.format("%s_%s.sql", Formats.LEXICAL_TIME_FORMATTER.format(Instant.now()), args[1]);
            File forward = new File("db/" + Migrations.FORWARD + "/" + filename);
            File reverse = new File("db/" + Migrations.REVERSE + "/" + filename);
            if(forward.exists() || reverse.exists()) {
                throw new RuntimeException("Forward or reverse file already exists");
            }
            try {
                forward.createNewFile();
                reverse.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException("IOException whne creating migration file", ex);
            }
            this.logger.info("Created " + forward.getName());
            this.logger.info("Created " + reverse.getName());
            shutdown();
            return true;
        } else if(args.length > 0) {
            this.logger.info("Error: unrecognized command line arguments");
            this.logger.info("with args:");
            this.logger.info("  migrate");
            this.logger.info("  migrate:reverse VERSION");
            this.logger.info("without args, runs web server");
        }

        return false;
    }

    /*
     * Return error on failure, null on success.
     */
    public String webSetup() {
        this.apiClientManager = new ApiClientManager();
        
        this.logger.info("Scanning for request handlers...");
        String error = findRequestHandlers();
        if(error != null) {
            return error;
        }
        this.logger.info("Creating thymeleaf template engine...");
        setupTemplateEngine();
        this.logger.info("Refreshing assets...");
        assetFingerprinter.refresh();
        
        return null;
    }

    public String findRequestHandlers() {
        Pair<List<RequestHandler>, String> scanResult = (new Scanner()).scan();
        if(scanResult.getValue1() != null) {
            return "Error when scanning for handlers: " + scanResult.getValue1();
        }
        this.requestHandlers = scanResult.getValue0();
        return null;
    }        

    public void shutdown() {
        this.logger.info("Shutting down database...");
        this.dbConf.shutdown();
    }

    public void setupTemplateEngine() {
        this.templateEngine = new TemplateEngine();
        templateEngine.addDialect(new LayoutDialect());

        FileTemplateResolver templateResolver = new FileTemplateResolver();
        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("web_app_views/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(this.config.env == "production");
        
        templateEngine.setTemplateResolver(templateResolver);
    }        

    private void loadConfig() {
        if(config != null) {
            return;
        }

        this.config = new Config();
        
        Dotenv dotenv = Dotenv.load();
        this.config.githubConfig = new GithubConfig(dotenv.get("GITHUB_CLIENT_ID"), dotenv.get("GITHUB_CLIENT_SECRET"));
        this.config.cookieSigningKey = dotenv.get("COOKIE_SIGNING_KEY");
        this.config.dbUrl = dotenv.get("DB_URL");
        this.config.env = dotenv.get("APP_ENV");
        if(this.config.env == null) {
            this.config.env = "development";
        }
        // db connection pool size?
    }

    private void runTomcat() {
        this.logger.info("Initializing tomcat...");
        EmbeddedTomcat embeddedTomcat = new EmbeddedTomcat();
        embeddedTomcat.prepare();

        this.logger.info("Starting tomcat...");
        embeddedTomcat.start();
    }

    private void runExp1() {
        UserRepository userRepository = this.dbConf.getRepository(this, UserRepository.class);
        Experiment ex = new Experiment();
        ex.run(userRepository);
    }

    public static void main(String[] args) {
        Application app = new Application();
        current = app;
        app.run(args);        
    }
}
