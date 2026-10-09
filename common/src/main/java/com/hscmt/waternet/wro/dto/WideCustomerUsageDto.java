package com.hscmt.waternet.wro.dto;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.common.BaseDto;
import com.hscmt.waternet.wro.domain.QWideCustomerUsage;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "광역수용가검침량정보")
@EqualsAndHashCode(callSuper = true)
public class WideCustomerUsageDto extends BaseDto {
    @Schema(description = "사용년월")
    private String useYm;
    @Schema(description = "계측기번호")
    private String mrnrNo;
    @Schema(description = "시설계측기번호")
    private String fcltyMrnrNo;
    @Schema(description = "수용가번호")
    private String cstmrNo;
    @Schema(description = "수용가명")
    private String cstmrNm;
    @Schema(description = "사용량")
    private Double mtUsgqty;
    @Schema(description = "계약시작일자")
    private String cntrctStatStrtDe;
    @Schema(description = "계약종료일자")
    private String cntrctStatEndDe;
    @Schema(description = "원본계약시작일자")
    private String oriCntrctStatStrtDe;
    @Schema(description = "원본계약종료일자")
    private String oriCntrctStatEndDe;

    public static List<Expression<?>> projectionFields (QWideCustomerUsage qWideCustomerUsage) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qWideCustomerUsage),
                List.of(
                        qWideCustomerUsage.key.useYm,
                        qWideCustomerUsage.key.mrnrNo,
                        qWideCustomerUsage.key.fcltyMrnrNo,
                        qWideCustomerUsage.cstmrNo,
                        qWideCustomerUsage.cstmrNm,
                        qWideCustomerUsage.mtUsgqty,
                        qWideCustomerUsage.cntrctStatStrtDe,
                        qWideCustomerUsage.cntrctStatEndDe,
                        qWideCustomerUsage.oriCntrctStatStrtDe,
                        qWideCustomerUsage.oriCntrctStatEndDe
                )
        );
    }
}
