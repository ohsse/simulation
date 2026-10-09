package com.hscmt.simulation.group.repository;

import com.hscmt.simulation.group.dto.GroupDto;

import java.util.List;

public interface ProgramGroupCustomRepository {

    List<GroupDto> findAllProgramGroups(String grpId);
}
