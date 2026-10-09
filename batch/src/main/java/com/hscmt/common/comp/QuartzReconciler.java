package com.hscmt.common.comp;

import com.hscmt.simulation.schedule.service.SchedulerManagedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzReconciler implements ApplicationRunner {

    private final Scheduler scheduler;
    private final SchedulerManagedService schedulerManagedService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /* 스케줄 정지 */
        scheduler.standby();
        /* 스케줄 job reconcile */
        schedulerManagedService.reconcileJobs();
        /* 스케줄러 시작 */
        scheduler.start();
    }

}
