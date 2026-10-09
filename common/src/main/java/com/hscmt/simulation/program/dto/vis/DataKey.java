package com.hscmt.simulation.program.dto.vis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "차트&그리드 조회대상")
@NoArgsConstructor
@Data
public class DataKey {
    @Schema(description = "파일명")
    private String fileName;
    @Schema(description = "sheet명")
    private String sheetName;
    @Schema(description = "헤더색인")
    private Integer headerIndex;
}
