package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "프로그램실행인수")
@NoArgsConstructor
@Data
public class ProgramRunArgDto {
    @Schema(description = "인수명", example = "--name")
    private String name;
    @Schema(description = "인수값")
    private String value;
}
