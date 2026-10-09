package com.hscmt.simulation.library.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.library.domain.QLibrary;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "파이썬 패키지 정보")
public class LibraryDto extends BaseDto {
    @Schema(description = "라이브러리_ID")
    private String lbrId;
    @Schema(description = "라이브러리명")
    private String lbrNm;
    @Schema(description = "라이브러리버전")
    private String lbrVrsn;
    @Schema(description = "파이썬버전")
    private String pyVrsn;
    @Schema(description = "원본파일명")
    private String ortxFileNm;

    public static List<Expression<?>> projectionFields (QLibrary qLibrary) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qLibrary.lbrId,
                        qLibrary.lbrNm,
                        qLibrary.lbrVrsn,
                        qLibrary.pyVrsn,
                        qLibrary.ortxFileNm
                ),
                BaseDto.getBaseFields(qLibrary)
        );
    }
}
