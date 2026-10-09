package com.hscmt.simulation.venv.event;

import com.hscmt.simulation.venv.comp.VenvManager;
import com.hscmt.simulation.venv.service.VenvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class VenvEventHandler {
    private final VenvService venvService;
    private final VenvManager venvManager;

    /* 등록 이벤트 */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVenvEvent(VenvCreatedEvent record) {
        log.info("가상환경 생성시작");
        venvManager.createVenv(record.venvId(), record.pyVrsn(), record.lbrIds());
        log.info("가상환경 및 패키지 생성완료");
    }

    /* 수정이벤트 */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVenvEvent (VenvUpdatedEvent record) {
        venvManager.updateVenv(record.venvId(), record.addLbrIds(), record.delLbrNms());
    }

    /* 삭제 이벤트 */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVenvEvent (VenvDeletedEvent record) {
        venvManager.deleteVenv(record.venvId());
    }
}
