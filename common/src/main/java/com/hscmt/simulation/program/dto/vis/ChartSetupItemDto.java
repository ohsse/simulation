package com.hscmt.simulation.program.dto.vis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hscmt.common.enumeration.VisTypeCd;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartSetupItemDto implements VisSetupItem{
    @Schema(description = "시각화유형", implementation = VisTypeCd.class)
    private String type = VisTypeCd.CHART.name();
    @Schema(description = "ID")
    private String id;
    @Schema(description = "라벨")
    private String label;
    @ArraySchema(schema = @Schema(implementation = ChartSetupChildItemDto.class))
    private List<ChartSetupChildItemDto> children = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = DataKey.class))
    private List<DataKey> targetList = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = ColorConditionDto.class))
    private List<ColorConditionDto> colorConditions = new ArrayList<>();
}
