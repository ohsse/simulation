package com.hscmt;

import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Component
public class ShutdownOrderGuard implements SmartLifecycle {
    private final Scheduler scheduler;
    private final TaskExecutor batchExecutor;
    private volatile boolean running = true;

    public ShutdownOrderGuard(
            Scheduler scheduler,
            @Qualifier("batchExecutor") TaskExecutor batchExecutor
    ) {
        this.scheduler = scheduler;
        this.batchExecutor = batchExecutor;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        try {
            // 1) Quartz 먼저 정지(실행 중 잡 완료 대기)
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
            }
        } catch (SchedulerException e) {
            // log.warn("Quartz shutdown failed", e);
        } finally {
            // 2) batchExecutor 안전 종료(런타임 타입에 따라 분기)
            try {
                if (batchExecutor instanceof ThreadPoolTaskExecutor tpe) {
                    // setWaitForTasksToCompleteOnShutdown / awaitTerminationSeconds 설정이 이미 반영됨
                    tpe.shutdown();
                } else if (batchExecutor instanceof ConcurrentTaskExecutor cte) {
                    Executor delegate = cte.getConcurrentExecutor();
                    if (delegate instanceof ExecutorService es) {
                        es.shutdown(); // 필요시 es.awaitTermination(...)
                    }
                } else {
                    // SimpleAsyncTaskExecutor 등 종료 개념이 없는 구현체는 noop
                    // log.info("batchExecutor does not support graceful shutdown: {}", batchExecutor.getClass().getName());
                }
            } catch (Exception e) {
                // log.warn("batchExecutor shutdown failed", e);
            }
        }
        running = false;
    }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isAutoStartup() { return true; }
    @Override public int getPhase() { return Integer.MAX_VALUE; } // 가장 마지막에 stop
}
