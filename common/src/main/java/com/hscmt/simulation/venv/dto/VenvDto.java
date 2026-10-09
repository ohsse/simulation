package com.hscmt.simulation.venv.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.venv.domain.QVirtualEnvironment;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "가상환경 정보")
public class VenvDto extends BaseDto {
    @Schema(description = "가상환경_ID")
    private String venvId;
    @Schema(description = "가상환경명")
    private String venvNm;
    @Schema(description = "가상환경설명")
    private String venvDesc;
    @Schema(description = "파이썬버전")
    private String pyVrsn;
    @Schema(description = "가상환경경로")
    private String venvPath;
    @Schema(description = "사용가능여부")
    private YesOrNo useAbleYn;
    @ArraySchema(schema = @Schema(description = "라이브러리목록", implementation = VenvLbrDto.class))
    private List<VenvLbrDto> lbrs = new ArrayList<>();

    public static List<Expression<?>> projectionFields (QVirtualEnvironment qVirtualEnvironment) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qVirtualEnvironment.venvId,
                        qVirtualEnvironment.venvNm,
                        qVirtualEnvironment.venvDesc,
                        qVirtualEnvironment.pyVrsn,
                        qVirtualEnvironment.useAbleYn
                ),
                BaseDto.getBaseFields(qVirtualEnvironment)
        );
    }

    public void addLbr (VenvLbrDto lbr) {
        this.lbrs.add(lbr);
    }

    public void addLbrs (List<VenvLbrDto> lbrs) {
        this.lbrs.addAll(lbrs);
    }
}
