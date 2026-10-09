package com.hscmt.simulation.dashboard.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dashboard.domain.QDashboard;
import com.hscmt.simulation.group.dto.GroupItemDto;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "대시보드정보")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashboardDto extends GroupItemDto {
    @Schema(description = "상황판ID")
    private String dsbdId;
    @Schema(description = "상황판명")
    private String dsbdNm;
    @Schema(description = "대시보드설명")
    private String dsbdDesc;
    @Schema(description = "해상도가로값")
    private Integer resWidthVal;
    @Schema(description = "해상도세로값")
    private Integer resHglnVal;
    @ArraySchema(schema = @Schema(implementation = DsbdVisItemDto.class))
    private List<DsbdVisItemDto> items;

    public static List<Expression<?>> projectionFields (QDashboard qDashboard) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qDashboard.dsbdId,
                        qDashboard.dsbdNm,
                        qDashboard.dsbdDesc,
                        qDashboard.resWidthVal,
                        qDashboard.resHglnVal,
                        qDashboard.grpId,
                        qDashboard.sortOrd,
                        qDashboard.items
                ),
                BaseDto.getBaseFields(qDashboard)
        );
    }
}
