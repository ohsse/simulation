package com.hscmt.waternet.tag.dto;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.tag.domain.child.QRwisHourData;
import com.hscmt.waternet.tag.domain.child.QRwisMinuteData;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
public class TagDataDto {
    @Schema(description = "태그번호")
    private Long tagsn;
    @Schema(description = "계측시간")
    private String logTime;
    @Schema(description = "값")
    private BigDecimal val;

    public static List<Expression<?>> projectionFields (EntityPathBase<?> q) {
        if (q instanceof QRwisMinuteData qMin) {
            return QProjectionUtil.getCombinedExpressions(
                    List.of(
                            qMin.id.tagsn,
                            qMin.id.logTime,
                            qMin.val
                    )
            );
        } else {
            QRwisHourData qHour = QRwisHourData.rwisHourData;
            return QProjectionUtil.getCombinedExpressions(
                    List.of(
                            qHour.id.tagsn,
                            qHour.id.logTime,
                            qHour.val
                    )
            );
        }
    }
}
