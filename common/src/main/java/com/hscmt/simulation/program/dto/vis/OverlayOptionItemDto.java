package com.hscmt.simulation.program.dto.vis;


import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "오버레이설정")
@NoArgsConstructor
@Data
public class OverlayOptionItemDto {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "절점명")
    private String label;
    @ArraySchema(schema = @Schema(description = "키목록",implementation = OverlayChildrenItemDto.class))
    private List<OverlayChildrenItemDto> children = new ArrayList<>();
}
