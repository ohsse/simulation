package com.hscmt.simulation.group.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.group.domain.LayerGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import com.hscmt.simulation.group.dto.GroupUpsertDto;
import com.hscmt.simulation.group.event.GroupEventPublisher;
import com.hscmt.simulation.group.repository.LayerGroupRepository;
import com.hscmt.simulation.layer.domain.Layer;
import com.hscmt.simulation.layer.dto.LayerUpsertDto;
import com.hscmt.simulation.layer.repository.LayerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@SimulationTx(readOnly = true)
public class LayerGroupService extends GroupBaseService<LayerGroup>{

    private final LayerGroupRepository layerGroupRepository;
    private final LayerRepository layerRepository;

    public LayerGroupService (LayerGroupRepository repository, LayerRepository itemRepository, GroupEventPublisher<LayerGroup> publisher) {
        super(repository, publisher);
        this.layerGroupRepository = repository;
        this.layerRepository = itemRepository;
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_LAYER)"
    )
    @SimulationTx
    public String upsertGroup (GroupUpsertDto group) {
        return super.upsertGroup(group);
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_LAYER)"
    )
    @SimulationTx
    public void upsertGroups(List<GroupUpsertDto> list) {
        super.upsertGroups(list);
    }

    @Override
    public List<LayerGroup> findAllGroupRecursive(List<String> ids) {
        return layerGroupRepository.findAllGroupRecursive(ids);
    }

    @Override
    public GroupDto getGroupsWithChildrenAndItems(String grpId) {
        return super.hierarchyWithItems(
                layerGroupRepository.findAllLayerGroups(grpId),
                new ArrayList<>(layerRepository.findAllLayers(grpId))
        );
    }

    @Override
    public LayerGroup createEntity(GroupUpsertDto dto) {
        return new LayerGroup(dto);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_LAYER)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(String grpId) {
        layerRepository.updateGrpIdToNull(grpId);
    }


    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_LAYER)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(List<String> grpIds) {
        layerRepository.updateGrpIdToNull(grpIds);
    }


    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_LAYER)"
    )
    @Override
    @SimulationTx
    public void updateGroupItems (List<GroupItemUpsertDto> items) {
        for (GroupItemUpsertDto item : items) {
            if (item instanceof LayerUpsertDto l) {
                layerRepository.findById(l.getLayerId())
                        .ifPresent(findLayer -> findLayer.changeGrpInfo(item.getGrpId(), item.getSortOrd()));
            }
        }
    }
}
