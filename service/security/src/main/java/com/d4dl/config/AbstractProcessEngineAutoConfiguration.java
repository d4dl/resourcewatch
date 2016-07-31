package com.d4dl.config;

import org.activiti.engine.*;
import org.activiti.spring.*;
import org.activiti.spring.boot.AbstractProcessEngineConfiguration;
import org.activiti.spring.boot.ActivitiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

/**
 * @See JpaProcessEngineAutoConfiguration
 */
public abstract class AbstractProcessEngineAutoConfiguration
        extends AbstractProcessEngineConfiguration {

    protected ActivitiProperties activitiProperties;

    @Autowired
    private ResourcePatternResolver resourceLoader;

    @Bean
    public SpringRejectedJobsHandler springRejectedJobsHandler() {
        return new SpringCallerRunsRejectedJobsHandler();
    }

    protected SpringProcessEngineConfiguration baseSpringProcessEngineConfiguration(DataSource dataSource, PlatformTransactionManager platformTransactionManager,
                                                                                    SpringAsyncExecutor springAsyncExecutor) throws IOException {

        List<Resource> procDefResources = this.discoverProcessDefinitionResources(
                this.resourceLoader, this.activitiProperties.getProcessDefinitionLocationPrefix(),
                this.activitiProperties.getProcessDefinitionLocationSuffix(),
                this.activitiProperties.isCheckProcessDefinitions());

        SpringProcessEngineConfiguration conf = super.processEngineConfigurationBean(
                procDefResources.toArray(new Resource[procDefResources.size()]), dataSource,
                platformTransactionManager, springAsyncExecutor);

        conf.setDeploymentName(defaultText(activitiProperties.getDeploymentName(), conf.getDeploymentName()));
        conf.setDatabaseSchema(defaultText(activitiProperties.getDatabaseSchema(), conf.getDatabaseSchema()));
        conf.setDatabaseSchemaUpdate(defaultText(activitiProperties.getDatabaseSchemaUpdate(), conf.getDatabaseSchemaUpdate()));

        conf.setJobExecutorActivate(activitiProperties.isJobExecutorActivate());
        conf.setAsyncExecutorEnabled(activitiProperties.isAsyncExecutorEnabled());
        conf.setAsyncExecutorActivate(activitiProperties.isAsyncExecutorActivate());

        conf.setMailServerHost(activitiProperties.getMailServerHost());
        conf.setMailServerPort(activitiProperties.getMailServerPort());
        conf.setMailServerUsername(activitiProperties.getMailServerUserName());
        conf.setMailServerPassword(activitiProperties.getMailServerPassword());
        conf.setMailServerDefaultFrom(activitiProperties.getMailServerDefaultFrom());
        conf.setMailServerUseSSL(activitiProperties.isMailServerUseSsl());
        conf.setMailServerUseTLS(activitiProperties.isMailServerUseTls());


        return conf;
    }


    private String defaultText(String deploymentName, String deploymentName1) {
        if (StringUtils.hasText(deploymentName))
            return deploymentName;
        return deploymentName1;
    }

    @Autowired
    protected void setActivitiProperties(ActivitiProperties activitiProperties) {
        this.activitiProperties = activitiProperties;
    }

    protected ActivitiProperties getActivitiProperties() {
        return this.activitiProperties;
    }


    @Bean
    public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration configuration) throws Exception {
        return super.springProcessEngineBean(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    @Override
    public RuntimeService runtimeServiceBean(ProcessEngine processEngine) {
        return super.runtimeServiceBean(processEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    @Override
    public RepositoryService repositoryServiceBean(ProcessEngine processEngine) {
        return super.repositoryServiceBean(processEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    @Override
    public TaskService taskServiceBean(ProcessEngine processEngine) {
        return super.taskServiceBean(processEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    @Override
    public HistoryService historyServiceBean(ProcessEngine processEngine) {
        return super.historyServiceBean(processEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    @Override
    public ManagementService managementServiceBeanBean(ProcessEngine processEngine) {
        return super.managementServiceBeanBean(processEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    @Override
    public FormService formServiceBean(ProcessEngine processEngine) {
        return super.formServiceBean(processEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    @Override
    public IdentityService identityServiceBean(ProcessEngine processEngine) {
        return super.identityServiceBean(processEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
