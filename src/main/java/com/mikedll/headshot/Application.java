package com.mikedll.headshot;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

@ComponentScan
public class Application {
    
    public static ConfigurableApplicationContext appCtx;
    
    public static void main(String[] args) {        
        loadDotEnv();
        
        System.out.println("Starting app in " + Env.env + " environment...");        

        SpringApplication springApplication = new SpringApplication(new Class<?>[] { Application.class });
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        Application.appCtx = springApplication.run(args);

        // System.out.println("jpaSharedEM_entityManagerFactory: " + appCtx.getBean("jpaSharedEM_entityManagerFactory"));
        // Application.appCtx = new AnnotationConfigApplicationContext(Application.class);

        EntityManagerFactory emf = (EntityManagerFactory)appCtx.getBean("entityManagerFactory");
        // EntityManager em = emf.createEntityManager();
        EntityManager em = (EntityManager)appCtx.getBean("jpaSharedEM_entityManagerFactory");
        JpaRepositoryFactory jrf = new JpaRepositoryFactory(em);
        UserRepository userRepository = jrf.getRepository(UserRepository.class, RepositoryFragments.empty());
        // UserRepository userRepository = Application.appCtx.getBean(UserRepository.class);

        // runTomcat();
        runExp1(userRepository);
        
        System.out.println("Closing application context...");
        appCtx.close();
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

    private static void printBeans() {
        for (String beanName : appCtx.getBeanDefinitionNames()) {
            System.out.println(beanName);
        }
        System.out.println("Done printing beans");        
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
}
