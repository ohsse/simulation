package com.hscmt.simulation.program.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.dto.GroupItemDto;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.domain.QProgramInputFile;
import com.hscmt.simulation.program.domain.QProgramResult;
import com.hscmt.simulation.program.domain.QProgramVisualization;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.group.GroupBy.list;


@Schema(description = "프로그램정보")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramDto extends GroupItemDto {
    @Schema(description = "프로그램ID")
    private String pgmId;
    @Schema(description = "프로그램명")
    private String pgmNm;
    @Schema(description = "프로그램설명")
    private String pgmDesc;
    @Schema(description = "정렬순서")
    private Integer sortOrd;
    @Schema(description = "그룹ID")
    private String grpId;
    @Schema(description = "가상환경ID")
    private String venvId;
    @Schema(description = "가상환경명")
    private String venvNm;
    @Schema(description = "실시간여부", implementation = YesOrNo.class)
    private YesOrNo rltmYn;
    @Schema(description = "실행반복주기유형", implementation = CycleCd.class)
    private CycleCd rpttIntvTypeCd;
    @Schema(description = "실행간격")
    private Integer rpttIntvVal;
    @Schema(description = "최초실행일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime strtExecDttm;
    @ArraySchema(schema = @Schema(implementation = ProgramArgDto.class, description = "프로그램실행인수목록"))
    private List<ProgramArgDto> pgmArgs = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = ProgramResultDto.class, description = "프로그램결과"))
    private List<ProgramResultDto> results = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = ProgramInputFileDto.class, description = "프로그램인풋파일"))
    private List<ProgramInputFileDto> inputFiles = new ArrayList<>();
    @ArraySchema(schema = @Schema(implementation = ProgramVisualizationDto.class, description = "프로그램시각화정보"))
    private List<ProgramVisualizationDto> visualizations = new ArrayList<>();
    @Schema(description = "프로그램 사용용량")
    private String pgmUseBytes;
    @Schema(description = "프로그램파일정보", implementation = FileInfoDto.class)
    private FileInfoDto pgmFileInfo;

    public static List<Expression<?>> projectionFields (QProgram qProgram) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qProgram),
                List.of(
                        qProgram.pgmId,
                        qProgram.pgmNm,
                        qProgram.pgmDesc,
                        qProgram.grpId,
                        qProgram.sortOrd,
                        qProgram.venvId,
                        qProgram.rltmYn,
                        qProgram.rpttIntvTypeCd,
                        qProgram.rpttIntvVal,
                        qProgram.strtExecDttm,
                        qProgram.pgmArgs
                )
        );
    }

    public static List<Expression<?>> projectionFields (QProgram qProgram, QProgramInputFile qProgramInputFile ) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgram),
                listInputFileFields(qProgramInputFile)
        );
    }

    public static List<Expression<?>> projectionFields (QProgram qProgram, QProgramVisualization qProgramVisualization) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgram),
                listVisualizationFields(qProgramVisualization)
        );
    }

    public static List<Expression<?>> projectionFields(QProgram qProgram, QProgramResult qProgramResult) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgram),
                listResultFields(qProgramResult)
        );
    }

    public static List<Expression<?>> projectionFields (QProgram qProgram, QProgramResult qProgramResult, QProgramVisualization qProgramVisualization) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgram),
                listResultFields(qProgramResult),
                listVisualizationFields(qProgramVisualization)
        );
    }

    public static List<Expression<?>> projectionFields (QProgram qProgram, QProgramResult qProgramResult, QProgramVisualization qProgramVisualization, QProgramInputFile qProgramInputFile) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgram),
                listInputFileFields(qProgramInputFile),
                listResultFields(qProgramResult),
                listVisualizationFields(qProgramVisualization)
        );
    }

    private static List<Expression<?>> listVisualizationFields(QProgramVisualization qProgramVisualization) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        list(
                                QProjectionUtil.toQBean(ProgramVisualizationDto.class, ProgramVisualizationDto.projectionFields(qProgramVisualization))
                        ).as("visualizations")
                )
        );
    }

    private static List<Expression<?>> listResultFields(QProgramResult qProgramResult) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        list(
                                QProjectionUtil.toQBean(ProgramResultDto.class, ProgramResultDto.projectionFields(qProgramResult))
                        ).as("results")
                )
        );
    }

    private static List<Expression<?>> listInputFileFields(QProgramInputFile qProgramInputFile) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        list(
                                QProjectionUtil.toQBean(ProgramInputFileDto.class, ProgramInputFileDto.projectionFields(qProgramInputFile))
                        ).as("inputFiles")
                )
        );
    }
}
