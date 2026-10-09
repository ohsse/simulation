package com.hscmt.simulation.dataset.event;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.cache.CacheKeyManager;
import com.hscmt.common.enumeration.JobTargetType;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.common.comp.BatchClientComp;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dataset.comp.DatasetFileManager;
import com.hscmt.simulation.job.ScheduleJobRequest;
import com.hscmt.simulation.program.service.ProgramInputFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class DatasetEventHandler {

    private final CacheKeyManager cacheManager;
    private final DatasetFileManager fileManager;
    private final ProgramInputFileService programInputFileService;
    private final BatchClientComp batchClientComp;
    private final JobTargetType JOB_GROUP = JobTargetType.DATASET;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerEvent (DatasetUpsertedEvent event) {
        /* 데이터 수집 시작 */
        batchClientComp.collectTags(event.dsId());

        /* 실시간 여부가 Y 이면 등록 : N 이면 삭제 */
        if (event.rltmYn() == YesOrNo.Y) {
            /* 데이터셋 quartz job 등록 */
            batchClientComp.upsertJob(
                    new ScheduleJobRequest(
                            JOB_GROUP,
                            event.dsId(),
                            event.rgstDttm().truncatedTo(ChronoUnit.MINUTES),
                            event.cycleCd(),
                            1
                    )
            );
        } else {
            /* 데이터셋 quartz job 삭제 */
            batchClientComp.deleteJob(JOB_GROUP, event.dsId());
            /* 데이터셋 파일만 생성 */
            batchClientComp.createDatasetFile(event.dsId());
        }


        /* 데이터셋 시각화 캐시 삭제 */
        cacheManager.evictByPrefixAllCaches(event.dsId());
        /* 데이터셋 그룹 캐시 삭제 */
        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_DATASET);
    }


    /* 데이터셋 정보 수정시 데이터셋 관련 캐시 초기화 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerEvent (DatasetUpdatedEvent event) {
        /* 데이터셋 시각화 캐시 삭제 */
        cacheManager.evictByPrefixAllCaches(event.dsId());
        /* 데이터셋 그룹 캐시 삭제 */
        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_DATASET);
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void handlerEvent (DatasetDeletedEvent event) {
        /* 프로그램 인풋 파일 삭제 */
        programInputFileService.deleteAllByDsId(event.dsId(), fileManager.getDatasetFileNames(event.dsId()));

        /* 데이터셋 삭제 시 데이터셋 폴더 삭제 */
        fileManager.deleteDatasetDir(event.dsId());
        
        /* 데이터셋 quartz job 삭제 */
        batchClientComp.deleteJob(JOB_GROUP, event.dsId());

        /* 데이터셋 시각화 캐시 삭제 */
        cacheManager.evictByPrefixAllCaches(event.dsId());
        /* 데이터셋 그룹 캐시 삭제 */
        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_DATASET);
    }
}
