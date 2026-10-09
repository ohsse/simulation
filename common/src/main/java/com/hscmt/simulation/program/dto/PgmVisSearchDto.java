package com.hscmt.simulation.program.dto;

import com.hscmt.common.enumeration.DirectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Schema(description = "프로그램시각화ID및데이터조회")
@Data
public class PgmVisSearchDto {
    @Schema(description = "시각화ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String visId;
    @Schema(description = "이력ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String histId;
    @Schema(description = "조회방향", implementation = DirectionType.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private DirectionType direction;
}
