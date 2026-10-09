package com.hscmt.simulation.program.dto.vis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hscmt.common.enumeration.ConditionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "색상조건정보")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ColorConditionDto {
    @Schema(description = "레이어속성명")
    private String property;
    @Schema(description = "기준값")
    private String standValue;
    @Schema(description = "조건유형", implementation = ConditionType.class)
    private ConditionType conditionType;
    @Schema(description = "색상값", example = "#000000")
    private String colorStr;
    @Schema(description = "우선도")
    private String priority;
    @Schema(description = "최소값(RANGE 전용)")
    private String rangeMin;
    @Schema(description = "최대값(RANGE 전용)")
    private String rangeMax;
    @Schema(description = "최소 포함여부(RANGE 전용)", defaultValue = "true")
    private Boolean minInclusive;
    @Schema(description = "최대 포함여부(RANGE 전용)", defaultValue = "false")
    private Boolean maxInclusive;
}
