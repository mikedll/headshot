package com.mikedll.headshot.db;

import java.lang.StackTraceElement;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;

import org.hibernate.cfg.AvailableSettings;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;

import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.orm.jpa.JpaTransactionManager;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;

import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

import com.mikedll.headshot.UserRepository;
import com.mikedll.headshot.Env;

@Configuration
public class DatabaseConfiguration {

    private static final String JTA_PLATFORM = "hibernate.transaction.jta.platform";

    public static HikariDataSource dataSource;

    public static LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    public static PlatformTransactionManager transactionManager;

    public static RepositoryProxyPostProcessor exceptionPostProcessor;

    public static RepositoryProxyPostProcessor transactionPostProcessor;
    
    public PlatformTransactionManager getTransactionManager() {
        if(this.transactionManager != null) {
            return this.transactionManager;
        }

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(getEntityManagerFactoryBean().getObject());
        this.transactionManager = transactionManager;
        return this.transactionManager;
    }

		DataSource getDataSource() {
        if(dataSource != null) {
            return dataSource;
        }
        
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(Env.dbUrl);
				dataSource.setPoolName("default");
        dataSource.setMaximumPoolSize(Env.poolSize);

        this.dataSource = dataSource;
        return this.dataSource;
		}

    public JpaVendorAdapter jpaVendorAdapter() {
    		AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(true);
        return adapter;
    }

    public RepositoryProxyPostProcessor getExceptionPostProcessor() {
        if(this.exceptionPostProcessor != null) {
            return this.exceptionPostProcessor;
        }

        this.exceptionPostProcessor = new PersistenceExceptionTranslationRepositoryProxyPostProcessor();
        return this.exceptionPostProcessor;
    }

    public RepositoryProxyPostProcessor getTransactionPostProcessor() {
        if(this.transactionPostProcessor != null) {
            return this.transactionPostProcessor;
        }

        this.transactionPostProcessor = new TransactionalRepositoryProxyPostProcessor(true);
        return this.transactionPostProcessor;
    }    

    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean() {
        if(this.entityManagerFactoryBean != null) {
            return entityManagerFactoryBean;
        }
        
        this.entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        
        this.entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
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

    public static UserRepository getUserRepository() {
        DatabaseConfiguration dbConf = new DatabaseConfiguration();
        EntityManagerFactory emf = dbConf.getEntityManagerFactoryBean().getObject();

        EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(emf);
        
        JpaRepositoryFactory jpaRepoFactory = new JpaRepositoryFactory(em);
        jpaRepoFactory.addRepositoryProxyPostProcessor(dbConf.getExceptionPostProcessor());
        jpaRepoFactory.addRepositoryProxyPostProcessor(dbConf.getTransactionPostProcessor());
        return jpaRepoFactory.getRepository(UserRepository.class, RepositoryFragments.empty());
    }
}
