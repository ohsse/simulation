package com.hscmt.simulation.collect.job.manual.config;

import com.hscmt.simulation.collect.dto.MsrmUpsertDto;
import com.hscmt.simulation.collect.job.manual.step.ClearTempFileTask;
import com.hscmt.simulation.collect.job.manual.step.TagManualCollectItemProcessor;
import com.hscmt.simulation.collect.job.manual.step.TagManualCollectItemReader;
import com.hscmt.simulation.collect.job.manual.step.TagManualCollectPartition;
import com.hscmt.simulation.collect.job.step.TagCollectItemWriter;
import com.hscmt.waternet.tag.dto.TagDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
public class TagManualCollectJobConfig {
    private final JobRepository jobRepository;
    @Qualifier("batchExecutor")
    private final TaskExecutor taskExecutor;
    @Qualifier("simulationTransactionManager")
    private final PlatformTransactionManager transactionManager;

    private final TagManualCollectPartition partition;

    private final TagManualCollectItemReader reader;
    private final TagManualCollectItemProcessor processor;
    private final TagCollectItemWriter writer;
    private final ClearTempFileTask clearTask;

    @Bean(name = "tagManualCollectJob")
    public Job tagManualCollectJob () {
        return new JobBuilder("tagManualCollectJob", jobRepository)
                .start(master())
                .next(clear())
                .build();
    }

    @Bean(name = "tagManualCollectMasterStep")
    public Step master() {
        return new StepBuilder("tagManualCollectMasterStep", jobRepository)
                .partitioner("tagManualCollectSlaveStep", partition)
                .step(slave())
                .gridSize(4)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean(name = "tagManualCollectSlaveStep")
    public Step slave() {
        return new StepBuilder("tagManualCollectSlaveStep", jobRepository)
                .<TagDataDto, MsrmUpsertDto> chunk (1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean(name = "tagManualCollectClearStep")
    public Step clear() {
        return new StepBuilder("tagManualCollectClearStep", jobRepository)
                .tasklet(clearTask, transactionManager)
                .build();
    }
}
