package com.mikedll.headshot.db;

import java.lang.StackTraceElement;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;

import com.zaxxer.hikari.HikariDataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;

import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;

import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.RepositoryRepository;
import com.mikedll.headshot.Config;
import com.mikedll.headshot.Application;
import com.mikedll.headshot.controller.Controller;

public class DatabaseConfiguration {

    private static final String JTA_PLATFORM = "hibernate.transaction.jta.platform";

    private Config config;

    private HikariDataSource dataSource;

    private LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    private PlatformTransactionManager transactionManager;

    private RepositoryProxyPostProcessor exceptionPostProcessor;

    private RepositoryProxyPostProcessor transactionPostProcessor;

    private JpaRepositoryFactory jpaRepositoryFactory;

    private Map<Class<?>, Object> repositories;

    public DatabaseConfiguration(Config config) {
        this.config = config;
        this.repositories = new HashMap<>();
    }
   
    public PlatformTransactionManager getTransactionManager() {
        if(this.transactionManager != null) {
            return this.transactionManager;
        }

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(getEntityManagerFactoryBean().getObject());

        this.transactionManager = transactionManager;
        return this.transactionManager;
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

    public RepositoryProxyPostProcessor getExceptionPostProcessor() {
        if(this.exceptionPostProcessor != null) {
            return this.exceptionPostProcessor;
        }

        this.exceptionPostProcessor = new PersistenceExceptionTranslationRepositoryProxyPostProcessor(getEntityManagerFactoryBean());
        return this.exceptionPostProcessor;
    }

    public RepositoryProxyPostProcessor getTransactionPostProcessor() {
        if(this.transactionPostProcessor != null) {
            return this.transactionPostProcessor;
        }

        this.transactionPostProcessor = new TransactionalRepositoryProxyPostProcessor(getTransactionManager(), true);
        return this.transactionPostProcessor;
    }    

    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean() {
        if(this.entityManagerFactoryBean != null) {
            return entityManagerFactoryBean;
        }

        this.entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

    		AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(this.config.env != "test");

        this.entityManagerFactoryBean.setJpaVendorAdapter(adapter);
        this.entityManagerFactoryBean.setDataSource(getDataSource());
        this.entityManagerFactoryBean.setPackagesToScan("com.mikedll.headshot");

        Map<String,Object> hibernateSettings = new LinkedHashMap<>();
        hibernateSettings.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        hibernateSettings.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
        hibernateSettings.put(AvailableSettings.SCANNER, "org.hibernate.boot.archive.scan.internal.DisabledScanner");
        
        hibernateSettings.put(JTA_PLATFORM, new NoJtaPlatform());
        this.entityManagerFactoryBean.getJpaPropertyMap().putAll(hibernateSettings);

        this.entityManagerFactoryBean.afterPropertiesSet();

        return this.entityManagerFactoryBean;
    }

    public JpaRepositoryFactory getJpaRepositoryFactory() {
        if(this.jpaRepositoryFactory != null) {
            return this.jpaRepositoryFactory;
        }

        EntityManagerFactory emf = getEntityManagerFactoryBean().getObject();
        EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(emf);
        this.jpaRepositoryFactory = new JpaRepositoryFactory(em);
        this.jpaRepositoryFactory.addRepositoryProxyPostProcessor(getExceptionPostProcessor());
        this.jpaRepositoryFactory.addRepositoryProxyPostProcessor(getTransactionPostProcessor());

        return this.jpaRepositoryFactory;
    }

    public void makeRepositories() {
        this.repositories.put(UserRepository.class, new UserRepository(this));
        this.repositories.put(RepositoryRepository.class, new RepositoryRepository(this));
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
