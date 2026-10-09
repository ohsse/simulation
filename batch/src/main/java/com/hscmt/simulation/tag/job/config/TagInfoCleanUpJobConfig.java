package com.hscmt.simulation.tag.job.config;

import com.hscmt.simulation.tag.job.step.CollectTagCheckStep;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class TagInfoCleanUpJobConfig {
    private final JobRepository jobRepository;

    @Qualifier("simulationTransactionManager")
    private final PlatformTransactionManager transactionManager;

    private final CollectTagCheckStep checkTask;


    @Bean(name = "tagInfoCleanUpJob")
    public Job tagInfoCleanUpJob () {
        return new JobBuilder("tagInfoCleanUpJob", jobRepository)
                .start(checkCollectTagStep())
                .build();
    }

    @Bean(name = "checkCollectTagStep")
    public Step checkCollectTagStep() {
        return new StepBuilder("checkCollectTagStep", jobRepository)
                .tasklet(checkTask, transactionManager)
                .build();
    }
}
