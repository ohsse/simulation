package com.hscmt.simulation.partition;

import com.hscmt.simulation.partition.spec.PartitionTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PartitionJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private Job partitionManageJob;

    @Autowired
    private Step partitionCreateStep;

    @Autowired
    private Step partitionDetachStep;

    @Autowired
    private Step programExecHistCleanupMasterStep;

    @Test
    public void 파티션생성테스트() throws Exception{
        PartitionTable targetTable = PartitionTable.MSRM_L;
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                .addString("jobName", partitionManageJob.getName())
                .addString("targetTableName", targetTable.name())
                .toJobParameters();

        Job testJob = new JobBuilder("testPartitionCreateJob", jobRepository)
                .start(partitionCreateStep)
                .build();

        JobExecution execution = jobLauncher.run(testJob, jobParameters);

        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());
    }

    @Test
    public void 파티션분리테스트() throws Exception{
        PartitionTable targetTable = PartitionTable.MSRM_L;
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                .addString("jobName", partitionManageJob.getName())
                .addString("targetTableName", targetTable.name())
                .toJobParameters();

        Job testJob = new JobBuilder("testPartitionDetachJob", jobRepository)
                .start(partitionDetachStep)
                .build();

        JobExecution execution = jobLauncher.run(testJob, jobParameters);

        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());
    }

    @Test
    public void 프로그램실행이력클린업테스트 () throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                .addString("jobName", partitionManageJob.getName())
                .toJobParameters();

        Job testJob = new JobBuilder("testProgramExecHistCleanupMasterStep", jobRepository)
                .start(programExecHistCleanupMasterStep)
                .build();

        JobExecution execution = jobLauncher.run(testJob, jobParameters);

        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());
    }

}
