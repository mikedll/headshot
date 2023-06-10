package com.mikedll.headshot;

import com.mikedll.headshot.db.DatabaseConfiguration;

import io.github.cdimascio.dotenv.Dotenv;

public class Application {
    
    public static void main(String[] args) {        
        loadDotEnv();
        
        System.out.println("Starting app in " + Env.env + " environment...");        

        // UserRepository userRepository = DatabaseConfiguration.getUserRepository();
        // runTomcat();
        // runExp1(userRepository);
        runExp2();

        if(DatabaseConfiguration.dataSource != null) {
            System.out.println("Closing data source...");
            DatabaseConfiguration.dataSource.close();
        }
    }

    private static void loadDotEnv() {
        Dotenv dotenv = Dotenv.load();
        Env.githubConfig = new GithubConfig(dotenv.get("GITHUB_CLIENT_ID"), dotenv.get("GITHUB_CLIENT_SECRET"));
        Env.cookieSigningKey = dotenv.get("COOKIE_SIGNING_KEY");
        Env.dbUrl = dotenv.get("DB_URL");
        Env.env = dotenv.get("APP_ENV");
        if(Env.env == null) {
            Env.env = "development";
        }
        // pool size?
    }

    private static void runTomcat() {
        System.out.println("Initializing tomcat...");
        EmbeddedTomcat embeddedTomcat = new EmbeddedTomcat();
        embeddedTomcat.prepare();

        System.out.println("Starting tomcat...");
        embeddedTomcat.start();
    }        

    private static void runExp1(UserRepository userRepository) {
        Experiment ex = new Experiment();
        ex.run(userRepository);
    }

    private static void runExp2() {
        Experiment2 ex = new Experiment2();
        ex.run();
    }
}
