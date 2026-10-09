package com.hscmt.simulation.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hscmt.common.enumeration.StructType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "대시보드 시각화 항목 정보")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DsbdVisItemDto {
    @Schema(description = "레이아웃설정", implementation = ItemLayoutDto.class)
    private ItemLayoutDto layout;
    @Schema(description = "항목유형", implementation = StructType.class)
    private StructType structType;
    @Schema(description = "항목값 (프로그램 시각화 ID or LabelText)")
    private String structValue;
    @Schema(description = "항목이름")
    private String structName;
}
