package com.hscmt.simulation.program.dto;

import com.hscmt.common.enumeration.MapFiletype;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "지도시각화설정")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class MapItemDto extends ProgramVisualizationItemDto {
    @Schema(description = "파일유형", implementation = MapFiletype.class)
    private MapFiletype fileType;
}
