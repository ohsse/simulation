package com.hscmt.simulation.group.service;

import com.hscmt.common.enumeration.GroupType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupServiceFactory {
    private final DatasetGroupService datasetGroupService;
    private final DashboardGroupService dashboardGroupService;
    private final ProgramGroupService programGroupService;
    private final LayerGroupService layerGroupService;

    public GroupBaseService<?> getService (GroupType groupType) {
        return switch (groupType) {
            case DATASET -> datasetGroupService;
            case DASHBOARD -> dashboardGroupService;
            case PROGRAM -> programGroupService;
            case LAYER -> layerGroupService;
        };
    }
}
