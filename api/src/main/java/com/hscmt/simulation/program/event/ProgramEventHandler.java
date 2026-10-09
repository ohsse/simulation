package com.hscmt.simulation.program.event;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.cache.CacheKeyManager;
import com.hscmt.common.enumeration.JobTargetType;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.common.comp.BatchClientComp;
import com.hscmt.simulation.job.ScheduleJobRequest;
import com.hscmt.simulation.layer.domain.LayerList;
import com.hscmt.simulation.layer.repository.LayerRepository;
import com.hscmt.simulation.layer.service.LayerService;
import com.hscmt.simulation.program.comp.ProgramFileManager;
import com.hscmt.simulation.program.repository.ProgramExecHistRepository;
import com.hscmt.simulation.program.repository.ProgramInputFileRepository;
import com.hscmt.simulation.program.repository.ProgramResultRepository;
import com.hscmt.simulation.program.repository.ProgramVisualizationRepository;
import com.hscmt.simulation.program.service.ProgramResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProgramEventHandler {

    private final ProgramFileManager fileManager;
    private final ProgramExecHistRepository programExecHistRepository;
    private final ProgramInputFileRepository programInputFileRepository;
    private final BatchClientComp batchClientComp;
    private final CacheKeyManager cacheManager;
    private final JobTargetType JOB_GROUP = JobTargetType.PROGRAM;

    /* 프로그램 삭제 트랜잭션 이후 파일 삭제 하는 로직 실행 */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(ProgramDeletedEvent event) {
        /* 파일 삭제 */
        fileManager.deleteProgramDir(event.pgmId());
        /* job 트리거 삭제 */
        batchClientComp.deleteJob(JOB_GROUP, event.pgmId());
    }

    /* 프로그램 삭제 이전에 프로그램을 참조하고 있는 엔터티들의 프로그램 ID 값을 초기화 하거나 매핑테이블 내용을 정리한다. */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeEvent (ProgramDeletedEvent event) {
        String pgmId = event.pgmId();
        /* 프로그램 실행이력 삭제 */
        programExecHistRepository.deleteAllByPgmId(pgmId);
        /* 프로그램 인풋 파일 삭제 */
        programInputFileRepository.deleteAllByPgmId(pgmId);
        /* 프로그램 캐시 클리어 */
        cacheClear(event.pgmId());
    }

    /* 프로그램 생성 및 수정 시 배치서버로 job event 발행 트랜잭션 벗어나서 비동기로 실행 */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent (ProgramUpsertedEvent event) {
        /* 실시간여부가 Y 면 job 등록 */
        if (event.rltmYn() == YesOrNo.Y) {
            batchClientComp.upsertJob(
                    new ScheduleJobRequest(
                            JOB_GROUP,
                            event.pgmId(),
                            event.strtExecDttm(),
                            event.cycleCd(),
                            event.interval()
                    )
            );
        } else {
            /* 실시간여부가 N 이면 job 삭제 */
            batchClientComp.deleteJob(JOB_GROUP, event.pgmId());
        }

        cacheClear(event.pgmId());

    }

    private void cacheClear (String pgmId) {
        /* 프로그램 시각화 캐시 삭제 */
        cacheManager.evictByPrefixAllCaches(pgmId);
        /* 프로그램 그룹 캐시 삭제 */
        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_PROGRAM);
    }
}
