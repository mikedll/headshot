package com.mikedll.headshot;

import java.util.ArrayList;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;

import org.javatuples.Pair;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.controller.Scanner;
import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.controller.RequestHandler;

public class Application {

    public static DatabaseConfiguration dbConf = new DatabaseConfiguration();

    public static List<RequestHandler> requestHandlers;
    
    public void run(String[] args) {
        loadDotEnv();

        System.out.println("Starting app in " + Env.env + " environment...");        

        String error = setUp();
        if(error != null) {
            System.out.println(error);
            return;
        }
        
        runTomcat();
        // runExp1();

        System.out.println("Shutting down database...");
        dbConf.shutdown();        
    }

    /*
     * Return error on failure, null on success.
     */
    private String setUp() {
        dbConf.makeRepositories();
        Pair<List<RequestHandler>, String> scanResult = (new Scanner()).scan();
        if(scanResult.getValue1() != null) {
            dbConf.shutdown();
            return "Error when scanning for handlers: " + scanResult.getValue1();
        }
        this.requestHandlers = scanResult.getValue0();
        Controller.setupTemplateEngine();
        return null;
    }

    private void loadDotEnv() {
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
