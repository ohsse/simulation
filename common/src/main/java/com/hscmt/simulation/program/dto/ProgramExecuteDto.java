package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "프로그램실행정보")
@Data
@NoArgsConstructor
public class ProgramExecuteDto {
    @Schema(description = "프로그램ID")
    private String pgmId;
    @ArraySchema(schema = @Schema(description = "프로그램실행인수", implementation = ProgramRunArgDto.class))
    private List<ProgramRunArgDto> args = new ArrayList<>();
}
