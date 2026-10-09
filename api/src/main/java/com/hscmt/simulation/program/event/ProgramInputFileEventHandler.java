package com.hscmt.simulation.program.event;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.cache.CacheKeyManager;
import com.hscmt.simulation.program.comp.ProgramInputFileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramInputFileEventHandler {
    private final ProgramInputFileManager fileManager;
    private final CacheKeyManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent (ProgramInputFileDeletedEvent event) {
        /* 프로그램 input 파일 레코드 삭제 시 프로그램 인풋파일 삭제 */
        fileManager.deleteInputFiles(event.pgmId(), event.fileNames());
        /* 프로그램 그룹 캐시 삭제 */
        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_PROGRAM);
        /* 프로그램 그룹 캐시 삭제 */
        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_DATASET);
    }
}
