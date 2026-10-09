package com.hscmt.simulation.dataset.job;

import com.hscmt.simulation.dataset.service.MeasureDatasetService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@NoArgsConstructor
@Slf4j
public class MeasureDatasetCreatorJob extends QuartzJobBean {

    private MeasureDatasetService measureDatasetService;

    @Autowired
    public void setMeasureDatasetService(MeasureDatasetService measureDatasetService) {
        this.measureDatasetService = measureDatasetService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap map = context.getMergedJobDataMap();
        String id = map.getString("id");

        try {
            log.info("realtime dataset execute job start : {}", id);
            measureDatasetService.createMeasureDatasetFile(id);
            log.info("realtime dataset execute job end : {}", id);
        } catch (Exception e) {
            log.error("realtime dataset execute job error : {}", e.getMessage());
            JobExecutionException jobExecutionException = new JobExecutionException(e);
            jobExecutionException.setRefireImmediately(false);
            throw jobExecutionException;
        }
    }
}
