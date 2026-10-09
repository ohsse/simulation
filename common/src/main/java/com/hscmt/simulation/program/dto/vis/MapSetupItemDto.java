package com.hscmt.simulation.program.dto.vis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hscmt.common.enumeration.VisTypeCd;
import com.hscmt.simulation.program.dto.SectionRange;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "지도시각화설정")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapSetupItemDto implements VisSetupItem{
    @Schema(description = "시각화유형", implementation = VisTypeCd.class)
    private String type = VisTypeCd.MAP.name();
    @Schema(description = "ID")
    private String id;
    @Schema(description = "라벨")
    private String label;
    @ArraySchema(schema = @Schema(implementation = MapSetupItemDto.class))
    private List<MapSetupItemDto> children = new ArrayList<>();
    @Schema(description = "레이어표출파일", pattern = "~.inp")
    private String layerFile;
    @Schema(description = "데이터표출파일", pattern = "~.rpt")
    private String dataFile;
    @ArraySchema(schema = @Schema(implementation = SectionRange.class))
    private List<SectionRange> resultDisplayOption = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = String.class, description = "레이어ID"))
    private List<String> layerIds = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = OverlayOptionItemDto.class))
    private List<OverlayOptionItemDto> overlayItems = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = DataKey.class))
    private List<DataKey> targetList = new ArrayList<>();
}
