package com.hscmt.simulation.program.dto.vis;

import com.hscmt.common.enumeration.VisTypeCd;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ChartResultItemDto implements VisResultItem{
    private String type = VisTypeCd.CHART.name();
    private Map<String, List<ChartAndGridDataDto>> data;
}
