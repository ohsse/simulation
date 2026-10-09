package com.hscmt.simulation.partition.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
@Slf4j
public class PartitionTableCheckRunner implements org.quartz.Job {

    private final JobLauncher jobLauncher;

    @Qualifier("partitionManageJob")
    private final Job partitionManageJob;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {

            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobExecutor", jobDataMap.getString("jobExecutor"))
                    .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                    .toJobParameters();

            jobLauncher.run(partitionManageJob, jobParameters);

        } catch (Exception e) {
            log.error("table partition check job error : {}", e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
