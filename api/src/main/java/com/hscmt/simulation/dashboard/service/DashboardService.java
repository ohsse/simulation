package com.hscmt.simulation.dashboard.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.cache.CacheKeyManager;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dashboard.domain.Dashboard;
import com.hscmt.simulation.dashboard.dto.DashboardDto;
import com.hscmt.simulation.dashboard.dto.DashboardUpsertDto;
import com.hscmt.simulation.dashboard.dto.DsbdVisItemDto;
import com.hscmt.simulation.dashboard.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
public class DashboardService {

    private final DashboardRepository repository;
    private final CacheKeyManager cacheManager;

    /* 추가 및 수정 */
    @SimulationTx
    public void upsert (DashboardUpsertDto dto) {
        repository.findById(dto.getDsbdId())
                .ifPresentOrElse(
                        saveDashboard -> {
                            saveDashboard.changeInfo (dto);
                        },
                        () -> {
                            repository.save(new Dashboard(dto));
                            cacheManager.evictByPrefixAllCaches(dto.getDsbdId());
                        }
                );

        cacheManager.evictByPrefix(CacheConst.CACHE_1DAY, CacheConst.GROUP_DASHBOARD);
    }

    /* 삭제 */
    @SimulationTx
    public void delete (String dsbdId) {
        repository.findById(dsbdId)
                .ifPresent(repository::delete);
    }

    /* 전체조회 */
    public List<DashboardDto> findAllDashboards (String grpId) {
        return repository.findAllDashboards(grpId);
    }

    /* 단건조회 */
    public DashboardDto findDashboard (String dsbdId) {
        return repository.findDashboardDtoById(dsbdId);
    }

    /* 대시보드 시각화 정보 업데이트 */
    @SimulationTx
    public void updateDsbdVisItemsByVisIds (List<String> visIds) {
        repository.bulkRemoveDynamicItemsByVisIds(visIds.toArray(new String[0]));
    }
}
