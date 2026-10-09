package com.hscmt.simulation.group.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dataset.dto.DatasetSearchDto;
import com.hscmt.simulation.dataset.repository.DatasetRepository;
import com.hscmt.simulation.dataset.service.DatasetService;
import com.hscmt.simulation.group.domain.DatasetGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import com.hscmt.simulation.group.dto.GroupUpsertDto;
import com.hscmt.simulation.group.event.GroupEventPublisher;
import com.hscmt.simulation.group.repository.DatasetGroupRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@SimulationTx(readOnly = true)
public class DatasetGroupService extends GroupBaseService <DatasetGroup> {

    private final DatasetGroupRepository datasetGroupRepository;
//    private final DatasetRepository datasetRepository;
    private final DatasetService datasetService;
    
    public DatasetGroupService(DatasetGroupRepository repository, DatasetService datasetService, GroupEventPublisher<DatasetGroup> publisher) {
        super(repository, publisher);
        this.datasetGroupRepository = repository;
        this.datasetService = datasetService;
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DATASET)"
    )
    @SimulationTx
    public String upsertGroup (GroupUpsertDto group) {
        return super.upsertGroup(group);
    }

    @Override
    @SimulationTx
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DATASET)"
    )
    public void updateGroupItems(List<GroupItemUpsertDto> items) {
        datasetService.updateGroupItems(items);
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DATASET)"
    )
    @SimulationTx
    public void upsertGroups (List<GroupUpsertDto> list) {
        super.upsertGroups(list);
    }

    @Override
    public List<DatasetGroup> findAllGroupRecursive(List<String> ids) {
        return datasetGroupRepository.findAllGroupRecursive(ids);
    }

    @Cacheable(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DATASET)"
    )
    @Override
    public GroupDto getGroupsWithChildrenAndItems(String grpId) {
        DatasetSearchDto dto = new DatasetSearchDto();
        dto.setGrpId(grpId);
        return super.hierarchyWithItems(datasetGroupRepository.findAllDatasetGroups(grpId)
                , new ArrayList<>(datasetService.getDatasetList(dto)));
    }


    @Override
    public DatasetGroup createEntity(GroupUpsertDto dto) {
        return new DatasetGroup(dto);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DATASET)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(String grpId) {
        datasetService.updateGrpIdToNull(grpId);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_DATASET)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(List<String> grpIds) {
        datasetService.updateGrpIdToNull(grpIds);
    }
}
