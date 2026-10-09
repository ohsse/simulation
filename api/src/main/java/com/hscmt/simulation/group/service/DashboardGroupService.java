package com.hscmt.simulation.group.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dashboard.dto.DashboardUpsertDto;
import com.hscmt.simulation.dashboard.repository.DashboardRepository;
import com.hscmt.simulation.group.domain.DashboardGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import com.hscmt.simulation.group.dto.GroupUpsertDto;
import com.hscmt.simulation.group.event.GroupEventPublisher;
import com.hscmt.simulation.group.repository.DashboardGroupRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@SimulationTx(readOnly = true)
public class DashboardGroupService extends GroupBaseService<DashboardGroup> {

    private final DashboardGroupRepository dashboardGroupRepository;
    private final DashboardRepository dashboardRepository;

    public DashboardGroupService (DashboardGroupRepository groupRepository, DashboardRepository groupItemRepository, GroupEventPublisher<DashboardGroup> publisher) {
        super(groupRepository, publisher);
        this.dashboardGroupRepository = groupRepository;
        this.dashboardRepository = groupItemRepository;
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DASHBOARD)"
    )
    @SimulationTx
    public String upsertGroup (GroupUpsertDto group) {
        return super.upsertGroup(group);
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DASHBOARD)"
    )
    @SimulationTx
    public void updateGroupItems(List<GroupItemUpsertDto> items) {
        for (GroupItemUpsertDto item : items) {
            if (item instanceof DashboardUpsertDto d) {
                dashboardRepository.findById(d.getDsbdId())
                        .ifPresent(findDashboard -> findDashboard.changeGrpInfo(item.getGrpId(), item.getSortOrd()));
            }
        }
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DASHBOARD)"
    )
    @SimulationTx
    public void upsertGroups (List<GroupUpsertDto> list) {
        super.upsertGroups(list);
    }

    @Override
    public List<DashboardGroup> findAllGroupRecursive(List<String> ids) {
        return dashboardGroupRepository.findAllGroupRecursive(ids);
    }

    @Cacheable(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DASHBOARD)"
    )
    @Override
    public GroupDto getGroupsWithChildrenAndItems(String grpId) {
        return super.hierarchyWithItems(dashboardGroupRepository.findAllDashboardGroups(grpId),
                new ArrayList<>(dashboardRepository.findAllDashboards(grpId))
        );
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DASHBOARD)"
    )
    @Override
    public DashboardGroup createEntity(GroupUpsertDto dto) {
        return new DashboardGroup(dto);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DASHBOARD)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(String grpId) {
        dashboardRepository.updateGrpIdToNull(grpId);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DASHBOARD)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(List<String> grpIds) {
        dashboardRepository.updateGrpIdToNull(grpIds);
    }
}
