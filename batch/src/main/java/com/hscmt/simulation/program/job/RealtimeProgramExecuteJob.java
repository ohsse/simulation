package com.hscmt.simulation.program.job;

import com.hscmt.simulation.program.service.ProgramExecuteService;
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
@Slf4j
@NoArgsConstructor
public class RealtimeProgramExecuteJob extends QuartzJobBean {

    private ProgramExecuteService programExecuteService;

    @Autowired
    public void setProgramExecuteService(ProgramExecuteService programExecuteService) {
        this.programExecuteService = programExecuteService;
    }

    /* Quartz job 프로그램 실행  */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap map = context.getMergedJobDataMap();
        String id = map.getString("id");

        try {
            log.info("realtime program execute job start : {}", id);
            programExecuteService.executeProgram(id);
            log.info("realtime program execute job end : {}", id);
        } catch (Exception e) {
            log.error("realtime program execute job error : {}", e.getMessage());
            JobExecutionException jobExecutionException = new JobExecutionException(e);
            jobExecutionException.setRefireImmediately(false);
            throw jobExecutionException;
        }
    }
}
