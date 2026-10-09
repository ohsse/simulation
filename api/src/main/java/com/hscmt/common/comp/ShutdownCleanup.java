package com.hscmt.common.comp;

import com.hscmt.common.util.ProcessUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShutdownCleanup {
    @PreDestroy
    public void destroy() {
        log.info("application 종료 : 프로세스 유틸 스레드 다운");
        ProcessUtil.shutdownExecutor();
    }
}
