package com.hscmt.simulation.common.config.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("simulationDataSource") DataSource dataSource, ApplicationContext context) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(dataSource);

        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory() {
            @Override
            protected Object createJobInstance (TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
                beanFactory.autowireBean(job);
                return job;
            }
        };
        schedulerFactoryBean.setJobFactory(jobFactory);

        schedulerFactoryBean.setQuartzProperties(getProps());
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContextKey");
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        return schedulerFactoryBean;
    }

    private static Properties getProps() {
        Properties props = new Properties();

        props.setProperty("org.quartz.scheduler.instanceName", "SimulationScheduler");
        props.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        props.setProperty("org.quartz.jobStore.class",
                "org.springframework.scheduling.quartz.LocalDataSourceJobStore");

        props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        props.setProperty("org.quartz.jobStore.isClustered", "true");
        props.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        props.setProperty("org.quartz.jobStore.misfireThreshold", "30000");

        props.setProperty("org.quartz.jobStore.driverDelegateClass","org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");

        props.setProperty("org.quartz.threadPool.threadCount", "32");
        props.setProperty("org.quartz.threadPool.threadPriority", "5");
        return props;
    }
}
