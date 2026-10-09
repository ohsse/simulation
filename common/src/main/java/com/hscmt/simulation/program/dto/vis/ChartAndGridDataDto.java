package com.hscmt.simulation.program.dto.vis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "차트|그리드 데이터")
@Data
public class ChartAndGridDataDto {
    @Schema(description = "데이터키", example = "result.xlsx|sheet1|header0")
    private String dataKey;
    @Schema(description = "값")
    private String value;
}
