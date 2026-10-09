package com.hscmt.simulation.layer.dto;

import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.group.dto.GroupItemDto;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "레이어정보저장")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class LayerUpsertDto extends GroupItemUpsertDto {
    @Schema(description = "레이어ID")
    private String layerId;
    @Schema(description = "레이어명")
    private String layerNm;
    @Schema(description = "레이어설명")
    private String layerDesc;
    @Schema(description = "초기표출여부", implementation = YesOrNo.class)
    private YesOrNo initDspyYn;
    @Schema(description = "좌표계", implementation = CrsyType.class)
    private CrsyType crsyTypeCd;
    @ArraySchema(schema = @Schema(description = "레이어스타일", implementation = LayerStyleInfo.class))
    private List<LayerStyleInfo> layerStyles;
}
