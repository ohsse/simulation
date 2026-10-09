package com.hscmt.simulation.collect.job.auto.config;

import com.hscmt.simulation.collect.dto.MsrmUpsertDto;
import com.hscmt.simulation.collect.dto.TagCollectDto;
import com.hscmt.simulation.collect.job.auto.step.TagAutoCollectItemProcessor;
import com.hscmt.simulation.collect.job.auto.step.TagAutoCollectPartition;
import com.hscmt.simulation.collect.job.auto.step.TagAutoCollectItemReader;
import com.hscmt.simulation.collect.job.step.TagCollectItemWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TagAutoCollectJobConfig {
    private final JobRepository jobRepository;
    @Qualifier("batchExecutor")
    private final TaskExecutor taskExecutor;
    @Qualifier("simulationTransactionManager")
    private final PlatformTransactionManager transactionManager;

    private final TagAutoCollectPartition partition;

    private final TagAutoCollectItemReader reader;
    private final TagAutoCollectItemProcessor processor;
    private final TagCollectItemWriter writer;

    @Bean(name = "tagAutoCollectJob")
    public Job tagAutoCollectJob () {
        return new JobBuilder("tagAutoCollectJob", jobRepository)
                .start(master())
                .build();
    }

    @Bean(name = "tagAutoCollectMasterStep")
    public Step master() {
        return new StepBuilder("tagAutoCollectMasterStep", jobRepository)
                .partitioner("tagAutoCollectSlaveStep", partition)
                .step(slave())
                .gridSize(4)
                .taskExecutor(taskExecutor)
                .build();

    }

    @Bean(name = "tagAutoCollectSlaveStep")
    public Step slave() {
        return new StepBuilder("tagAutoCollectSlaveStep", jobRepository)
                . <TagCollectDto, MsrmUpsertDto> chunk (1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
