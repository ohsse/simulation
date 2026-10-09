package com.hscmt.simulation.partition.job.config;

import com.hscmt.simulation.partition.job.config.partition.ProgramExecHistItemReader;
import com.hscmt.simulation.partition.job.config.partition.ProgramExecHistItemWriter;
import com.hscmt.simulation.partition.job.config.partition.ProgramExecHistPartitioner;
import com.hscmt.simulation.partition.job.config.task.PartitionTableCreateTasklet;
import com.hscmt.simulation.partition.job.config.task.PartitionTableDetachTasklet;
import com.hscmt.simulation.partition.job.config.task.PartitionTableDropTasklet;
import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class PartitionTableJobConfig {

    private final JobRepository jobRepository;

    @Qualifier("simulationTransactionManager")
    private final PlatformTransactionManager transactionManager;

    private final PartitionTableCreateTasklet createTask;
    private final PartitionTableDetachTasklet detachTask;
    private final PartitionTableDropTasklet dropTask;
    private final ProgramExecHistPartitioner partitioner;
    private final ProgramExecHistItemReader reader;
    private final ProgramExecHistItemWriter writer;

    @Bean(name = "partitionManageJob")
    public Job partitionManageJob() {
        return new JobBuilder("partitionManageJob", jobRepository)
                .start(partitionCreateStep())
                .next(partitionDetachStep())
                .next(programExecHistCleanupMasterStep())
                .next(partitionDropStep())
                .build();
    }

    @Bean(name = "partitionCreateStep")
    public Step partitionCreateStep () {
        return new StepBuilder("partitionCreateStep", jobRepository)
                .tasklet(createTask, transactionManager)
                .build();
    }

    @Bean(name = "partitionDetachStep")
    public Step partitionDetachStep() {
        return new StepBuilder("partitionDetachStep", jobRepository)
                .tasklet(detachTask, transactionManager)
                .build();
    }

    @Bean(name = "partitionDropStep")
    public Step partitionDropStep() {
        return new StepBuilder("partitionDropStep", jobRepository)
                .tasklet(dropTask, transactionManager)
                .build();
    }

    @Bean(name = "programExecHistCleanupMasterStep")
    public Step programExecHistCleanupMasterStep() {
        return new StepBuilder("programExecHistCleanupMasterStep", jobRepository)
                .partitioner("programExecHistCleanupSlaveStep", partitioner)
                .step(programExecHistCleanupSlaveStep())
                .build();
    }

    @Bean(name = "programExecHistCleanupSlaveStep")
    public Step programExecHistCleanupSlaveStep() {
        return new StepBuilder("programExecHistCleanupSlaveStep", jobRepository)
                .<ProgramExecHistDto, ProgramExecHistDto>chunk(1000, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }


//    @Bean(name = "partitionDropMasterStep")
//    public Step partitionDropMasterStep() {
//
//    }

}
