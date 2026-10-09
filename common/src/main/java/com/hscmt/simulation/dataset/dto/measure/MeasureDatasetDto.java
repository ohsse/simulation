package com.hscmt.simulation.dataset.dto.measure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import static com.querydsl.core.group.GroupBy.*;

import com.hscmt.simulation.dataset.domain.QMeasureDataset;
import com.hscmt.simulation.dataset.domain.QMeasureDatasetDetail;
import com.hscmt.simulation.dataset.domain.QWaternetTag;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "계측데이터셋 정보")
@EqualsAndHashCode(callSuper = true)
public class MeasureDatasetDto extends DatasetDto {
    @Schema(description = "실시간여부", implementation = YesOrNo.class)
    private YesOrNo rltmYn;
    @Schema(description = "시작일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime strtDttm;
    @Schema(description = "종료일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDttm;
    @Schema(description = "생성주기", implementation = CycleCd.class)
    private CycleCd termTypeCd;
    @Schema(description = "조회기간")
    private Integer inqyTerm;
    @ArraySchema(schema = @Schema(implementation = MeasureDatasetDetailDto.class, description = "계측데이터셋상세"))
    private List<MeasureDatasetDetailDto> detailItems = new ArrayList<>();

    public static List<Expression<?>> projectionFields (QMeasureDataset qMeasureDataset) {

        return QProjectionUtil.getCombinedExpressions(
                DatasetDto.projectionFields(qMeasureDataset._super),
                List.of(
                        qMeasureDataset.rltmYn,
                        qMeasureDataset.strtDttm,
                        qMeasureDataset.endDttm,
                        qMeasureDataset.termTypeCd,
                        qMeasureDataset.inqyTerm,
                        ExpressionUtils.as(Expressions.constant(DatasetType.MEASURE), "dsTypeCd")
                )
        );
    }

    public static List<Expression<?>> projectionFieldsWithDetailList (QMeasureDataset qMeasureDataset, QMeasureDatasetDetail qMeasureDatasetDetail) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qMeasureDataset),
                List.of(
                        list(
                                QProjectionUtil.toQBean(MeasureDatasetDetailDto.class, MeasureDatasetDetailDto.projectionFields(qMeasureDatasetDetail))
                        ).as("detailItems")
                )
        );
    }

    public static List<Expression<?>> projectionFieldsWithDetailList (QMeasureDataset qMeasureDataset, QMeasureDatasetDetail qMeasureDatasetDetail, QWaternetTag qWaternetTag) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qMeasureDataset),
                List.of(
                        list(
                                QProjectionUtil.toQBean(MeasureDatasetDetailDto.class, MeasureDatasetDetailDto.projectionFields(qMeasureDatasetDetail, qWaternetTag))
                        ).as("detailItems")
                )
        );
    }
}
