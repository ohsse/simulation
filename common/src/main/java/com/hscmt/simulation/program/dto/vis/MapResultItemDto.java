package com.hscmt.simulation.program.dto.vis;

import com.hscmt.common.enumeration.VisTypeCd;
import com.hscmt.simulation.program.dto.PipeNetworkReportDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class MapResultItemDto implements VisResultItem{
    private String type = VisTypeCd.MAP.name();
    private Map<String, Object> layerData;
    private PipeNetworkReportDto reportResult;
    private Map<String, List<ChartAndGridDataDto>> overlayResult;
}
