package com.hscmt.simulation.group.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.group.domain.ProgramGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import com.hscmt.simulation.group.dto.GroupUpsertDto;
import com.hscmt.simulation.group.event.GroupEventPublisher;
import com.hscmt.simulation.group.repository.ProgramGroupRepository;
import com.hscmt.simulation.program.repository.ProgramRepository;
import com.hscmt.simulation.program.service.ProgramService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@SimulationTx(readOnly = true)
public class ProgramGroupService extends GroupBaseService<ProgramGroup> {

    private final ProgramGroupRepository programGroupRepository;
    private final ProgramService programService;

    public ProgramGroupService (ProgramGroupRepository repository, ProgramService programService, GroupEventPublisher<ProgramGroup> publisher) {
        super(repository, publisher);
        this.programGroupRepository = repository;
        this.programService = programService;
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_PROGRAM)"
    )
    @SimulationTx
    public String upsertGroup (GroupUpsertDto group) {
        return super.upsertGroup(group);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_PROGRAM)"
    )
    @Override
    @SimulationTx
    public void updateGroupItems(List<GroupItemUpsertDto> items) {
        programService.updateGroupItems(items);
    }

    @Override
    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_PROGRAM)"
    )
    @SimulationTx
    public void upsertGroups (List<GroupUpsertDto> list) {
        super.upsertGroups(list);
    }

    @Override
    public List<ProgramGroup> findAllGroupRecursive(List<String> ids) {
        return programGroupRepository.findAllGroupRecursive(ids);
    }

    @Cacheable(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_PROGRAM)"
    )
    @Override
    public GroupDto getGroupsWithChildrenAndItems(String grpId) {
        return super.hierarchyWithItems(programGroupRepository.findAllProgramGroups(grpId)
                , new ArrayList<>(programService.findAllPrograms(grpId)));
    }


    @Override
    public ProgramGroup createEntity(GroupUpsertDto dto) {
        return new ProgramGroup(dto);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_PROGRAM)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(String grpId) {
        programService.updateGrpIdToNull(grpId);
    }

    @CacheEvict(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).GROUP_PROGRAM)"
    )
    @Override
    @SimulationTx
    public void updateGroupIdToNull(List<String> grpIds) {
        programService.updateGrpIdToNull(grpIds);
    }
}
