package com.hscmt.simulation.collect.job;

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
import java.time.temporal.ChronoUnit;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
@Slf4j
public class TagAutoCollectJobRunner implements org.quartz.Job {

    private final JobLauncher jobLauncher;
    @Qualifier("tagAutoCollectJob")
    private final Job tagAutoCollectJob;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap map = context.getMergedJobDataMap();
            String jobExecutor = map.getString("jobExecutor");
            int timeGapMinutes = map.getInt("timeGapMinutes");

            JobParameters parameters = new JobParametersBuilder()
                    .addString("jobExecutor", jobExecutor)
                    .addJobParameter("targetDateTime", LocalDateTime.now().minusMinutes(timeGapMinutes).truncatedTo(ChronoUnit.MINUTES), LocalDateTime.class)
                    .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                    .toJobParameters();

            jobLauncher.run(tagAutoCollectJob, parameters);
        } catch (Exception e) {
            log.error("TagAutoCollectJobRunner error : {}", e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
