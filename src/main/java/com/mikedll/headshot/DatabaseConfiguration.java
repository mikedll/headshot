package com.mikedll.headshot;

import java.lang.StackTraceElement;
import java.util.Map;
import java.util.LinkedHashMap;

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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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

import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.PlatformTransactionManagerCustomizer;
import org.springframework.boot.autoconfigure.transaction.TransactionProperties;

@EnableJpaRepositories
@Configuration
public class DatabaseConfiguration {

    private static final String JTA_PLATFORM = "hibernate.transaction.jta.platform";

    public static HikariDataSource dataSource;
    
    @Bean
    PlatformTransactionManager transactionManager(ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(transactionManager));
        return transactionManager;
    }

    @Bean
    public TransactionManagerCustomizers
        platformTransactionManagerCustomizers(ObjectProvider<PlatformTransactionManagerCustomizer<?>> customizers) {
        return new TransactionManagerCustomizers(customizers.orderedStream().toList());
    }

    @Bean
    TransactionProperties transactionProperties() {
        return new TransactionProperties();
    }

		DataSource getDataSource() {
        if(dataSource != null) {
            return dataSource;
        }
        
        System.out.println("Data source bean called and a data source is being constructed");
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

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        
        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        entityManagerFactoryBean.setDataSource(getDataSource());
        entityManagerFactoryBean.setPackagesToScan("com.mikedll.headshot");

        Map<String,Object> hibernateSettings = new LinkedHashMap<>();
        hibernateSettings.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        hibernateSettings.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
        hibernateSettings.put(AvailableSettings.SCANNER, "org.hibernate.boot.archive.scan.internal.DisabledScanner");
        hibernateSettings.put(JTA_PLATFORM, new NoJtaPlatform());
        entityManagerFactoryBean.getJpaPropertyMap().putAll(hibernateSettings);
        
        return entityManagerFactoryBean;
    }

    public static UserRepository getUserRepository() {
        EntityManagerFactory emf = (EntityManagerFactory)Application.appCtx.getBean("entityManagerFactory");
        
        // simulate bean: jpaSharedEM_entityManagerFactory. emf is a proxy around the emf bean.
        EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(emf);
        
        JpaRepositoryFactory jrf = new JpaRepositoryFactory(em);
        return jrf.getRepository(UserRepository.class, RepositoryFragments.empty());
    }
}
