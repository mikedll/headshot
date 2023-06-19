package com.mikedll.headshot;

import java.util.ArrayList;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;
import org.javatuples.Pair;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.Migrations;
import com.mikedll.headshot.controller.Scanner;
import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.controller.RequestHandler;
import com.mikedll.headshot.model.UserRepository;

public class Application {

    public static DatabaseConfiguration dbConf = new DatabaseConfiguration();

    public static List<RequestHandler> requestHandlers;

    public static AssetFingerprinter assetFingerprinter = new AssetFingerprinter();

    private boolean loadedEnv;

    public  static TemplateEngine templateEngine;

    public void run(String[] args) {
        ConfigurationFactory.setConfigurationFactory(new LoggingConfigFactory());

        if(argInterception(args)) {
            return;
        }

        String error = setUp();
        if(error != null) {
            System.out.println(error);
            shutdown();
            return;
        }

        runTomcat();
        // runExp1();

        shutdown();
    }

    public boolean argInterception(String[] args) {
        if(args.length == 1 && args[0].equals("migrate")) {
            loadDotEnv();
            Migrations migrations = new Migrations(dbConf.getDataSource());
            String error = migrations.readMigrations();
            if(error != null) {
                System.out.println("Error: " + error);
                shutdown();
                return true;
            }
            error = migrations.migrateForward();
            if(error != null) {
                System.out.println("Error: " + error);
            }
            shutdown();
            return true;
        } else if(args.length == 2 && args[0].equals("migrate:reverse")) {
            loadDotEnv();
            Migrations migrations = new Migrations(dbConf.getDataSource());
            String error = migrations.readMigrations();
            if(error != null) {
                System.out.println("Error: " + error);
                shutdown();
                return true;
            }
            error = migrations.reverse(args[1]);
            if(error != null) {
                System.out.println("Error: " + error);
            }
            shutdown();
            return true;
        } else if(args.length > 0) {
            System.out.println("Error: unrecognized command line arguments");
            System.out.println("with args:");
            System.out.println("  migrate");
            System.out.println("  migrate:reverse VERSION");
            System.out.println("without args, runs web server");
        }

        return false;
    }

    /*
     * Return error on failure, null on success.
     */
    public String setUp() {
        loadDotEnv();
        if(Env.shouldLog()) {
            System.out.println("Starting app in " + Env.env + " environment...");
        }
        
        System.out.println("Making repositories...");
        dbConf.makeRepositories();

        return postDbSetup();
    }

    /*
     * Mostly here to help with tests that don't need the database.
     */
    public String postDbSetup() {
        System.out.println("Scanning for request handlers...");
        String error = findRequestHandlers();
        if(error != null) {
            return error;
        }
        System.out.println("Creating thymeleaf template engine...");
        setupTemplateEngine();
        System.out.println("Refreshing assets...");
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
        System.out.println("Shutting down database...");
        dbConf.shutdown();        
    }

    public void markEnvLoaded() {
        this.loadedEnv = true;
    }

    public void setupTemplateEngine() {
        this.templateEngine = new TemplateEngine();
        templateEngine.addDialect(new LayoutDialect());

        FileTemplateResolver templateResolver = new FileTemplateResolver();
        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("web_app_views/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(Env.env == "production");
        
        templateEngine.setTemplateResolver(templateResolver);
    }        

    private void loadDotEnv() {
        if(loadedEnv) {
            return;
        }
        
        Dotenv dotenv = Dotenv.load();
        Env.githubConfig = new GithubConfig(dotenv.get("GITHUB_CLIENT_ID"), dotenv.get("GITHUB_CLIENT_SECRET"));
        Env.cookieSigningKey = dotenv.get("COOKIE_SIGNING_KEY");
        Env.dbUrl = dotenv.get("DB_URL");
        Env.env = dotenv.get("APP_ENV");
        if(Env.env == null) {
            Env.env = "development";
        }
        // db connection pool size?
    }

    private void runTomcat() {
        System.out.println("Initializing tomcat...");
        EmbeddedTomcat embeddedTomcat = new EmbeddedTomcat();
        embeddedTomcat.prepare();

        System.out.println("Starting tomcat...");
        embeddedTomcat.start();
    }        

    private void runExp1() {
        UserRepository userRepository = dbConf.getRepository(this, UserRepository.class);
        Experiment ex = new Experiment();
        ex.run(userRepository);
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);        
    }    
}
