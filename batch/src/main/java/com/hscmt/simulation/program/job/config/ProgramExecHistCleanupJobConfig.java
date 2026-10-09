package com.hscmt.simulation.program.job.config;

import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import com.querydsl.core.annotations.Config;
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
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ProgramExecHistCleanupJobConfig {
    private final JobRepository jobRepository;

    @Qualifier("batchExecutor")
    private final TaskExecutor taskExecutor;
    @Qualifier("simulationTransactionManager")
    private final PlatformTransactionManager transactionManager;

    private final ProgramExecHistCleanupPartitioner partition;
    private final ProgramExecHistCleanupItemReader reader;
    private final ProgramExecHistCleanUpItemWriter writer;

    @Bean(name="pgmExecHistCleanupJob")
    public Job pgmExecHistCleanupJob () {
        return new JobBuilder("pgmExecHistCleanupJob", jobRepository)
                .start(pgmExecHistCleanupMasterStep())
                .build();
    }

    @Bean(name = "pgmExecHistCleanupMasterStep")
    public Step pgmExecHistCleanupMasterStep() {
        return new StepBuilder("pgmExecHistCleanupMasterStep", jobRepository)
                .partitioner("pgmExecHistCleanupSlaveStep", partition)
                .step(pgmExecHistCleanupSlaveStep())
                .gridSize(12)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean(name = "pgmExecHistCleanupSlaveStep")
    public Step pgmExecHistCleanupSlaveStep () {
        return new StepBuilder("pgmExecHistCleanupSlaveStep", jobRepository)
                .<ProgramExecHistDto, ProgramExecHistDto>chunk(500, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }
}
