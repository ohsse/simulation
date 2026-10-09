package com.hscmt.simulation.layer.event;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.cache.CacheKeyManager;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.BatchClientComp;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.layer.dto.LayerUpsertRequest;
import com.hscmt.simulation.layer.service.LayerManageService;
import com.hscmt.simulation.program.service.ProgramVisualizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class LayerEventHandler {

    private final VirtualEnvironmentComponent vComp;
    private final LayerManageService layerManageService;
    private final BatchClientComp clientComp;
    private final CacheKeyManager cacheManager;
    private final ProgramVisualizationService visualizationService;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void handleEvent (LayerUpsertedEvent event) {
        File layerDir = new File(vComp.getLayerBasePath(), event.layerId());

        if (layerDir.exists()) {
            /* 레이어 관련 레코드 삭제 */
            layerManageService.deleteAllByLayerId(event.layerId());
            /* 레이어 파일 to Db 작업 */
            clientComp.saveLayerFileToDb(new LayerUpsertRequest(event.layerId(), event.crsyType(), event.styleInfo(), event.executorId()));
        }
        /* 캐시정리 */
        clearCache(event.layerId());
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void handleEvent (LayerDeletedEvent event) {
        /* 레이어 내역 전체 삭제 */
        layerManageService.deleteAllByLayerId(event.layerId());
        /* 레이어 관련 파일 삭제 */
        try {
            FileUtil.retryDelete(FileUtil.getDirPath(vComp.getLayerBasePath(), event.layerId()));
        } catch (Exception e) {
            log.error("file delete error : {} ", e.getMessage());
        }
        /* 캐시정리 */
        clearCache(event.layerId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleEventBefore (LayerDeletedEvent event) {
        visualizationService.bulkUpdateLayerIdsByResultId(event.layerId());
    }

    private void clearCache (String layerId) {
        /* 특정 레이어 Id 캐시삭제 */
        cacheManager.evictByPrefixAllCaches(layerId);
        /* 레이어 그룹 캐시삭제 */
        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_LAYER);
    }
}
