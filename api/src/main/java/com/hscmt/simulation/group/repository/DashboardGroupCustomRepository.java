package com.hscmt.simulation.group.repository;

import com.hscmt.simulation.group.dto.GroupDto;

import java.util.List;

public interface DashboardGroupCustomRepository {
    List<GroupDto> findAllDashboardGroups (String grpId);
}
