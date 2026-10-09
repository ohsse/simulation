package com.hscmt.simulation.layer.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.common.enumeration.FeatureType;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.domain.QLayerGroup;
import com.hscmt.simulation.group.dto.GroupItemDto;
import com.hscmt.simulation.layer.domain.QLayer;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "레이어정보")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class LayerDto extends GroupItemDto {
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
    private List<LayerStyleInfo> layerStyles = new ArrayList<>();
    @Schema(description = "레이어객체유형", implementation = FeatureType.class)
    private FeatureType layerFtype;
    @Schema(description = "레이어파일목록")
    private List<FileInfoDto> fileList = new ArrayList<>();
    @Schema(description = "사용가능여부", implementation = YesOrNo.class)
    private YesOrNo useAbleYn;
    @Schema(description = "프로퍼티목록")
    private List<String> properties = new ArrayList<>();

    public static List<Expression<?>> projectionFields (QLayer qLayer) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qLayer),
                List.of(
                        qLayer.layerId,
                        qLayer.layerNm,
                        qLayer.layerDesc,
                        qLayer.initDspyYn,
                        qLayer.crsyTypeCd,
                        qLayer.layerFtype,
                        qLayer.layerStyles,
                        qLayer.grpId,
                        qLayer.sortOrd,
                        qLayer.useAbleYn,
                        qLayer.properties
                )
        );
    }
}
