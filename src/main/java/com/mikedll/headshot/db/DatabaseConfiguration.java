package com.mikedll.headshot.db;

import java.lang.StackTraceElement;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.RepositoryRepository;
import com.mikedll.headshot.Config;
import com.mikedll.headshot.Application;
import com.mikedll.headshot.controller.Controller;

public class DatabaseConfiguration {

    private static final String JTA_PLATFORM = "hibernate.transaction.jta.platform";

    private Config config;

    private HikariDataSource dataSource;

    private Map<Class<?>, Object> repositories;

    public Logger logger;

    public DatabaseConfiguration(Config config) {
        this.logger = LogManager.getLogger("com.mikedll.headshot.db.DatabaseConfiguration");
        this.config = config;
        this.repositories = new HashMap<>();

        this.repositories.put(UserRepository.class, new UserRepository(this));
        this.repositories.put(RepositoryRepository.class, new RepositoryRepository(this));        
    }
   
    /*
     * Only tests should call this. For some reason,
     * my changes to the database aren't working in tests.
     */
    public HikariDataSource buildDataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(this.config.dbUrl);
				dataSource.setPoolName("default");
        dataSource.setMaximumPoolSize(this.config.poolSize);
        dataSource.setAutoCommit(false);
        return dataSource;
    }
    
		public DataSource getDataSource() {
        if(this.dataSource != null) {
            return this.dataSource;
        }

        this.dataSource = buildDataSource();
        return this.dataSource;
		}

    public void makeRepositories() {
    }
    
    public <T> T getRepository(Application app, Class<T> repositoryClass) {
        if(this.repositories.get(repositoryClass) == null) {
            throw new RuntimeException("Request for repository that does not exist: " + repositoryClass);
        }

        return repositoryClass.cast(this.repositories.get(repositoryClass));
    }   

    public <T> T getRepository(Controller controller, Class<T> repositoryClass) {
        if(!controller.canAccessData()) {
            throw new RuntimeException("Controller canAccessData() returned false when getting repository");
        }
        
        if(this.repositories.get(repositoryClass) == null) {
            throw new RuntimeException("Request for repository that does not exist: " + repositoryClass);
        }

        return repositoryClass.cast(this.repositories.get(repositoryClass));
    }
    
    public void shutdown() {
        if(dataSource != null) {
            dataSource.close();
        }
    }
}
