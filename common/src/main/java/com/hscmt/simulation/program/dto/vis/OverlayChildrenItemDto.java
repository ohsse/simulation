package com.hscmt.simulation.program.dto.vis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "오버레이정보 자식객체")
public class OverlayChildrenItemDto {
    @Schema(description = "id")
    private String id;
    @Schema(description = "라벨")
    private String label;
    @Schema(description = "데이터키", example = "fileName|sheetName|headerIndex")
    private String dataKey;
}
