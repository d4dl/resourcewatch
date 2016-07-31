package com.d4dl.config;

import java.io.IOException;


import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ActivitiProperties;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Copied directly from package org.activiti.spring.boot
 * AbstractProcessEngineAutoConfiguration expects to be able to Autowire a TaskExecutor bean.
 * But when the websockets dependency was added the app could not be started because
 * there were 4 candidate taskexecutor beans registered.  So I copied added an annotation
 * to the main application disabling using that class to autoconfigure.  Instead, this
 * copied class is used to autoconfigure but it creates a TaskExecutor explicitly instead of expecting
 * an autowired one.
 */
@Configuration
@AutoConfigureBefore(DataSourceProcessEngineAutoConfiguration.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class JpaProcessEngineAutoConfiguration {

    @Configuration
    @ConditionalOnClass(name= "javax.persistence.EntityManagerFactory")
    @EnableConfigurationProperties(ActivitiProperties.class)
    public static class JpaConfiguration extends AbstractProcessEngineAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Bean
        @ConditionalOnMissingBean
        public SpringProcessEngineConfiguration springProcessEngineConfiguration(
                DataSource dataSource, EntityManagerFactory entityManagerFactory,
                PlatformTransactionManager transactionManager, SpringAsyncExecutor springAsyncExecutor) throws IOException {

            SpringProcessEngineConfiguration config = this.baseSpringProcessEngineConfiguration(dataSource,
                    transactionManager, springAsyncExecutor);
            config.setJpaEntityManagerFactory(entityManagerFactory);
            config.setTransactionManager(transactionManager);
            config.setJpaHandleTransaction(false);
            config.setJpaCloseEntityManager(false);
            return config;
        }
    }

}
