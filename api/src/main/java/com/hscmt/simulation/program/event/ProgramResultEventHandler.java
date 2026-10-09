package com.hscmt.simulation.program.event;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.cache.CacheKeyManager;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.simulation.layer.service.LayerService;
import com.hscmt.simulation.program.comp.ProgramFileManager;
import com.hscmt.simulation.program.service.ProgramInputFileService;
import com.hscmt.simulation.program.service.ProgramVisualizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramResultEventHandler {

    private final ProgramInputFileService inputFileService;
    private final ProgramVisualizationService visualizationService;
    private final ProgramFileManager fileManager;
    private final LayerService layerService;
    private final CacheKeyManager cacheKeyManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async(value = "asyncExecutor")
    public void handleEventAfter (ProgramResultDeletedEvent event) {
        /* 프로그램 실행결과로 생성된 모든 파일 삭제 */
        fileManager.deleteAllResultFiles(event.pgmId(), event.rsltNm(), event.fileXtns());
        /* 프로그램 그룹 캐시 삭제 */
        cacheKeyManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_PROGRAM);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleEventBefore (ProgramResultDeletedEvent event) {
        // 프로그램 인풋파일로 쓰는 파일을 삭제한다.
        List<String> resultFileNames = new ArrayList<>();
        event.fileXtns().getValidExtensions()
                .stream()
                .forEach( ext -> {
                    resultFileNames.add(event.rsltNm() + ext);
                });
        /* 프로그램 인풋파일로 설정된 모든 파일 삭제 */
        inputFileService.deleteAllByRsltId(event.rsltId(), resultFileNames);

        /* 이미지 프로그램 결과 일때, 이미지 시각화에서도 삭제 */
        if (event.fileXtns() != FileExtension.SHP) {
            List<String> extensions = event.fileXtns().getValidExtensions();

            for (String ext : extensions) {
                visualizationService.deleteByResultFileName (event.rsltNm() + ext);
            }
        }

        /* shape 프로그램 결과 일때, 레이어 관리에서도 삭제 */
        if (event.fileXtns() == FileExtension.SHP) {
            /* 레이어 삭제 */
            layerService.deleteLayer( event.rsltId() );
        }

        /* 프로그램 그룹 캐시 삭제 */
        cacheKeyManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_PROGRAM);
    }
}
