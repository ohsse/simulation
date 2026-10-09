package com.hscmt.simulation.dataset.dto;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QWaternetTag;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "워터넷 태그 정보")
public class WaternetTagDto {
    @Schema(description = "태그번호")
    private String tagSn;
    @Schema(description = "태그유형코드")
    private String tagSeCd;
    @Schema(description = "태그설명")
    private String tagDesc;
    @Schema(description = "사용여부", implementation = YesOrNo.class)
    private YesOrNo useYn;

    public static List<Expression<?>> projectionFields (QWaternetTag qWaternetTag) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qWaternetTag.tagSn,
                        qWaternetTag.tagSeCd,
                        qWaternetTag.tagDesc,
                        qWaternetTag.useYn
                )
        );
    }
}
