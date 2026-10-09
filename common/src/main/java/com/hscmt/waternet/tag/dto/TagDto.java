package com.hscmt.waternet.tag.dto;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.tag.domain.QIfTag;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "워터넷 태그 마스터")
public class TagDto {
    @Schema(description = "태그일련번호")
    private String tagSn;
    @Schema(description = "태그유형코드")
    private String tagSeCd;
    @Schema(description = "태그설명")
    private String tagDesc;
    @Schema(description = "태그별칭")
    private String tagAlias;

    public static List<Expression<?>> projectionFields(QIfTag qIfTag) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qIfTag.tagSn,
                        qIfTag.tagDesc,
                        qIfTag.tagAlias,
                        qIfTag.tagSeCd
                )
        );
    }
}
