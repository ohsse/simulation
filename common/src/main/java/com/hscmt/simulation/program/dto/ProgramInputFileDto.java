package com.hscmt.simulation.program.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.enumeration.InputFileType;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QDataset;
import com.hscmt.simulation.program.domain.QProgramInputFile;
import com.hscmt.simulation.program.domain.QProgramResult;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "프로그램 인풋파일 정보")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProgramInputFileDto extends BaseDto {
    @Schema(description = "프로그램입력파일ID")
    private String inputFileId;
    @Schema(description = "프로그램ID")
    private String pgmId;
    @Schema(description = "대상ID")
    private String trgtId;
    @Schema(description = "대상명")
    private String trgtNm;
    @Schema(description = "대상유형코드", implementation = InputFileType.class)
    private InputFileType trgtType;
    @Schema(description = "파일유형", implementation = FileExtension.class)
    private FileExtension fileXtns;

    public static List<Expression<?>> projectionFields(QProgramInputFile qProgramInputFile) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qProgramInputFile.inputFileId,
                        qProgramInputFile.pgmId,
                        qProgramInputFile.trgtType,
                        qProgramInputFile.trgtId
                ),
                BaseDto.getBaseFields(qProgramInputFile)
        );
    }

    public static List<Expression<?>> projectionFields(QProgramInputFile qProgramInputFile, QDataset qDataset) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgramInputFile),
                List.of(
                        qDataset.dsNm.as("trgtNm"),
                        qDataset.fileXtns
                )
        );
    }

    public static List<Expression<?>> projectionFields(QProgramInputFile qProgramInputFile, QProgramResult qProgramResult) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgramInputFile),
                List.of(
                        qProgramResult.rsltNm.as("trgtNm"),
                        qProgramResult.fileXtns
                )
        );
    }
}
