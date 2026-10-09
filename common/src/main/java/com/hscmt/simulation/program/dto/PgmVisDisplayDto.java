package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
@Schema(description = "프로그램시각화표출")
public class PgmVisDisplayDto {
    @Schema(description = "시각화설정")
    private Map<String, Object> visSetupText;
}
