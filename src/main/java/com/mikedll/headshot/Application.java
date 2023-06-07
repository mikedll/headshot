package com.mikedll.headshot;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ApplicationContext;

@ComponentScan
public class Application {

    public static ApplicationContext appCtx;
    
    public static void main(String[] args) {        
        Dotenv dotenv = Dotenv.load();
        Env.githubConfig = new GithubConfig(dotenv.get("GITHUB_CLIENT_ID"), dotenv.get("GITHUB_CLIENT_SECRET"));
        Env.cookieSigningKey = dotenv.get("COOKIE_SIGNING_KEY");
        Env.dbUrl = dotenv.get("DB_URL");
        Env.env = dotenv.get("APP_ENV");
        if(Env.env == null) {
            Env.env = "development";
        }

        System.out.println("Starting app in " + Env.env + " environment...");        
        
        Application.appCtx = new AnnotationConfigApplicationContext(Application.class);
        // for (String beanName : appCtx.getBeanDefinitionNames()) {
        //     System.out.println(beanName);
        // }
        // System.out.println("Done printing beans");
        
        System.out.println("Initializing tomcat...");
        EmbeddedTomcat embeddedTomcat = new EmbeddedTomcat();
        embeddedTomcat.prepare();

        System.out.println("Starting tomcat...");
        embeddedTomcat.start();
    }
    
}
