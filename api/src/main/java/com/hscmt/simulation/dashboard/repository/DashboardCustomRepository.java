package com.hscmt.simulation.dashboard.repository;

import com.hscmt.simulation.dashboard.domain.Dashboard;
import com.hscmt.simulation.dashboard.dto.DashboardDto;

import java.util.List;

public interface DashboardCustomRepository {
    List<DashboardDto> findAllDashboards(String grpId);
    DashboardDto findDashboardDtoById (String dsId);
    void updateGrpIdToNull (String grpId);
    void updateGrpIdToNull (List<String> grpIds);
    List<Dashboard> findAllDashboardByVisIds (List<String> visIds);
}
