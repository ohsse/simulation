package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "프로그램시각화항목정보")
@NoArgsConstructor
@Data
public class ProgramVisualizationItemDto {
    private String fileName;
}
