package com.hscmt.simulation.program.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.program.domain.QProgramResult;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "프로그램결과정보")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class ProgramResultDto extends BaseDto {
    @Schema(description = "프로그램결과_ID")
    private String rsltId;
    @Schema(description = "프로그램ID")
    private String pgmId;
    @Schema(description = "결과명")
    private String rsltNm;
    @Schema(description = "파일확장자", implementation = FileExtension.class)
    private FileExtension fileXtns;

    public static List<Expression<?>> projectionFields (QProgramResult qProgramResult) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qProgramResult.rsltId,
                        qProgramResult.pgmId,
                        qProgramResult.rsltNm,
                        qProgramResult.fileXtns
                ),
                BaseDto.getBaseFields(qProgramResult)
        );
    }
}
