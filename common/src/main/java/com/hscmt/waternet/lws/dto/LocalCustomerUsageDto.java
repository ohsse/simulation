package com.hscmt.waternet.lws.dto;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.common.BaseDto;
import com.hscmt.waternet.lws.domain.QLocalCustomer;
import com.hscmt.waternet.lws.domain.QLocalCustomerUsage;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "지방수용가검침량 정보")
@EqualsAndHashCode(callSuper = true)
public class LocalCustomerUsageDto extends BaseDto {
    @Schema(description = "검침년월", format = "yyyy-MM", example = "2024-12")
    private String stym;
    @Schema(description = "수용가번호", example = "2038471")
    private String dmno;
    @Schema(description = "지자체연계코드", example = "35180")
    private String sgccd;
    @Schema(description = "대블록명", example = "대블록1")
    private String lfcltyNm;
    @Schema(description = "중블록명", example = "중블록2")
    private String mfcltyNm;
    @Schema(description = "소블록명", example = "소블록3")
    private String sfcltyNm;
    @Schema(description = "수용가명", example = "박*선")
    private String dmnm;
    @Schema(description = "수용가주소", example = "할로할로")
    private String dmaddr;
    @Schema(description = "원본검침년월", example = "202405", format = "yyyyMM")
    private String oriStym;
    @Schema(description = "검침량", example = "30487")
    private Double wsusevol;
    @Schema(description = "조정량", example = "30487")
    private Double wsstvol;

    public static List<Expression<?>> projectionFields(QLocalCustomerUsage qLocalCustomerUsage, QLocalCustomer qLocalCustomer) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qLocalCustomer),
                List.of(
                        qLocalCustomerUsage.key.stym,
                        qLocalCustomerUsage.key.dmno,
                        qLocalCustomerUsage.oriStym,
                        qLocalCustomer.dmnm,
                        qLocalCustomer.dmaddr,
                        qLocalCustomer.lfcltyNm,
                        qLocalCustomer.mfcltyNm,
                        qLocalCustomer.sfcltyNm,
                        qLocalCustomer.sgccd,
                        qLocalCustomerUsage.wsstvol,
                        qLocalCustomerUsage.wsusevol
                )
        );
    }
}
