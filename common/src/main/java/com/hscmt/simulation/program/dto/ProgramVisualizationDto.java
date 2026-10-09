package com.hscmt.simulation.program.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.enumeration.VisTypeCd;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.program.domain.QProgramVisualization;
import com.hscmt.simulation.program.dto.vis.VisSetupItem;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Schema(description = "프로그램 시각화 정보")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramVisualizationDto extends BaseDto {
    @Schema(description = "프로그램시각화_ID")
    private String visId;
    @Schema(description = "프로그램시각화명")
    private String visNm;
    @Schema(description = "프로그램_ID")
    private String pgmId;
    @Schema(description = "시각화유형", implementation = VisTypeCd.class)
    private VisTypeCd visTypeCd;
    @Schema(description = "시각화설정")
    private VisSetupItem visSetupText;
//    private Map<String, Object> visSetupText;

    public static List<Expression<?>> projectionFields (QProgramVisualization qProgramVisualization) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qProgramVisualization),
                List.of(
                        qProgramVisualization.visId,
                        qProgramVisualization.visNm,
                        qProgramVisualization.pgmId,
                        qProgramVisualization.visTypeCd,
                        qProgramVisualization.visSetupText
                )
        );
    }
}
