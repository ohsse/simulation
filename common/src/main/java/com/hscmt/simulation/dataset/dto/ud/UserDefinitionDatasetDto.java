package com.hscmt.simulation.dataset.dto.ud;

import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QUserDefinitionDataset;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "사용자 정의 데이터셋 정보")
@EqualsAndHashCode(callSuper = true)
public class UserDefinitionDatasetDto extends DatasetDto {

    public static List<Expression<?>> projectionFields (QUserDefinitionDataset qUserDefinitionDataset) {
        return QProjectionUtil.getCombinedExpressions(
                DatasetDto.projectionFields(qUserDefinitionDataset._super),
                List.of(
                        ExpressionUtils.as(Expressions.constant(DatasetType.USER_DEF), "dsTypeCd")
                )
        );
    }
}
