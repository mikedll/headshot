package com.mikedll.headshot;

import io.github.cdimascio.dotenv.Dotenv;

import com.mikedll.headshot.db.DatabaseConfiguration;

public class Application {

    private DatabaseConfiguration dbConf = new DatabaseConfiguration();
    
    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);        
    }

    public void run(String[] args) {
        loadDotEnv();

        System.out.println("Starting app in " + Env.env + " environment...");        

        dbConf.makeRepositories();

        runTomcat();
        // app.runExp1(userRepository);

        System.out.println("Shutting down database...");
        dbConf.shutdown();        
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

    private void runExp1(UserRepository userRepository) {
        Experiment ex = new Experiment();
        ex.run(userRepository);
    }
}
