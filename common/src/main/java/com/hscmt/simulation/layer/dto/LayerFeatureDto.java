package com.hscmt.simulation.layer.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.layer.domain.QLayer;
import com.hscmt.simulation.layer.domain.QLayerList;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
@Schema(description = "레이어객체정보")
@EqualsAndHashCode(callSuper = true)
public class LayerFeatureDto extends BaseDto{
    @Schema(description = "레이어ID")
    private String layerId;
    @Schema(description = "레이어명")
    private String layerNm;
    @Schema(description = "객체타입")
    private String ftype;
    @Schema(description = "객체ID")
    private Long fid;
    @Schema(description = "좌표정보")
    private String gmtrVal;
    @Schema(description = "속성정보")
    private Map<String, Object> property;
    @Schema(description = "색상정보")
    private String colorStr;

    public static List<Expression<?>> projectionFields (QLayerList qLayerList, QLayer qLayer) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qLayerList),
                List.of(
                        qLayerList.id.layerId,
                        qLayerList.id.fid,
                        qLayerList.id.ftype.stringValue().as("ftype"),
                        qLayer.layerNm,
                        qLayerList.property,
                        qLayerList.colorStr,
                        Expressions.stringTemplate("ST_AsText({0})", qLayerList.gmtrVal).as("gmtrVal")
                )
        );
    }

    public static List<Expression<?>> projectionContractionFields (QLayerList qLayerList, QLayer qLayer) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qLayerList.id.layerId,
                        qLayerList.id.fid,
                        qLayerList.id.ftype.stringValue().as("ftype"),
                        Expressions.stringTemplate("ST_AsText({0})", qLayerList.gmtrVal).as("gmtrVal"),
                        qLayerList.colorStr,
                        qLayer.layerNm
                )
        );
    }
}