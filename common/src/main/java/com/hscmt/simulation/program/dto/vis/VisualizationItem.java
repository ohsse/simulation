package com.hscmt.simulation.program.dto.vis;

import com.hscmt.common.enumeration.ExecutionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class VisualizationItem {
    private VisSetupItem setup;
    private VisResultItem result;
    private String histId;
    private String visId;
    private LocalDateTime execStrtDttm;
    private LocalDateTime execEndDttm;
    private ExecutionType execTypeCd;
}
