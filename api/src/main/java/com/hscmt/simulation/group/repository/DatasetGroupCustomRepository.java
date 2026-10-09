package com.hscmt.simulation.group.repository;

import com.hscmt.simulation.group.dto.GroupDto;

import java.util.List;

public interface DatasetGroupCustomRepository {

    List<GroupDto> findAllDatasetGroups(String grpId);
}
