package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "프로그램 실행 인수")
public class ProgramArgDto {
    @Schema(description = "인수명", example = "--name")
    String name;
    @Schema(description = "인수설명", example = "이름")
    String comment;
}
