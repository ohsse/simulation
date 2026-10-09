package com.hscmt.simulation.dataset.dto.measure;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QMeasureDatasetDetail;
import com.hscmt.simulation.dataset.domain.QWaternetTag;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "계측데이터셋 상세 정보")
@EqualsAndHashCode(callSuper = true)
public class MeasureDatasetDetailDto extends BaseDto {
    @Schema(description = "상세항목_ID")
    private String dsItmId;
    @Schema(description = "데이터셋_ID")
    private String dsId;
    @Schema(description = "태그번호")
    private String tagSn;
    @Schema(description = "태그설명")
    private String tagDesc;
    @Schema(description = "태그유형코드")
    private String tagSeCd;
    @Schema(description = "정렬순서")
    private Integer sortOrd;

    public static List<Expression<?>> projectionFields (QMeasureDatasetDetail qMeasureDatasetDetail) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qMeasureDatasetDetail),
                List.of(
                        qMeasureDatasetDetail.dsItmId,
                        qMeasureDatasetDetail.tagSn,
                        qMeasureDatasetDetail.sortOrd,
                        qMeasureDatasetDetail.dataset.dsId
                )
        );
    }

    public static List<Expression<?>> projectionFields (QMeasureDatasetDetail qMeasureDatasetDetail, QWaternetTag qWaternetTag) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qMeasureDatasetDetail),
                List.of(
                        qMeasureDatasetDetail.dsItmId,
                        qMeasureDatasetDetail.tagSn,
                        qMeasureDatasetDetail.sortOrd,
                        qWaternetTag.tagDesc,
                        qWaternetTag.tagSeCd,
                        qMeasureDatasetDetail.dataset.dsId
                )
        );
    }
}
