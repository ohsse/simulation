package com.hscmt.waternet.lws.dto;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.common.BaseDto;
import com.hscmt.waternet.lws.domain.QLocalCivilApplicant;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "지방민원결과")
@EqualsAndHashCode(callSuper = true)
public class LocalCivilApplicantDto extends BaseDto {
    @Schema(description = "민원번호")
    private String cano;
    @Schema(description = "민원대분류코드")
    private String calrgcd;
    @Schema(description = "민원중분류코드")
    private String camidcd;
    @Schema(description = "민원대분류명")
    private String calrgnm;
    @Schema(description = "민원중분류명")
    private String camidnm;
    @Schema(description = "민원신청일자")
    private String caappldt;
    @Schema(description = "민원처리일자")
    private String prcsdt;
    @Schema(description = "민원접수일자")
    private String supdt;
    @Schema(description = "민원처리결과")
    private String caprcsrslt;

    public static List<Expression<?>> projectionFields (QLocalCivilApplicant qLocalCivilApplicant) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qLocalCivilApplicant),
                List.of(
                        qLocalCivilApplicant.cano,
                        qLocalCivilApplicant.calrgcd,
                        qLocalCivilApplicant.calrgnm,
                        qLocalCivilApplicant.camidcd,
                        qLocalCivilApplicant.camidnm,
                        qLocalCivilApplicant.caappldt,
                        qLocalCivilApplicant.prcsdt,
                        qLocalCivilApplicant.supdt,
                        qLocalCivilApplicant.caprcsrslt
                )
        );
    }
}
