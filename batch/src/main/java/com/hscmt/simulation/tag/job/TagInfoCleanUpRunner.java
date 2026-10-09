package com.hscmt.simulation.tag.job;

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
public class TagInfoCleanUpRunner implements org.quartz.Job {
    private final JobLauncher jobLauncher;

    @Qualifier("tagInfoCleanUpJob")
    private final Job tagInfoCleanUpJob;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobExecutor", jobDataMap.getString("jobExecutor"))
                .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                .toJobParameters();

        try {
            jobLauncher.run(tagInfoCleanUpJob, jobParameters);
        } catch (Exception e) {
            log.error("tagInfoCleanUpRunner error : {}", e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
