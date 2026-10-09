package com.hscmt.simulation.dataset.dto.pn;

import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QPipeNetworkDataset;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "관망데이터셋 정보")
@EqualsAndHashCode(callSuper = true)
public class PipeNetworkDatasetDto extends DatasetDto {
    @Schema(description = "좌표계", implementation = CrsyType.class)
    private CrsyType crsyTypeCd;

    public static List<Expression<?>> projectionFields (QPipeNetworkDataset qPipeNetworkDataset) {
        return QProjectionUtil.getCombinedExpressions(DatasetDto.projectionFields(qPipeNetworkDataset._super),
                List.of(
                        qPipeNetworkDataset.crsyTypeCd,
                        ExpressionUtils.as(Expressions.constant(DatasetType.PIPE_NETWORK), "dsTypeCd")
                )
        );
    }
}
