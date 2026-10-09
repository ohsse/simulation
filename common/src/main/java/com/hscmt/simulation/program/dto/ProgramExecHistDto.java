package com.hscmt.simulation.program.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.common.enumeration.ExecutionType;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.domain.QProgramExecHist;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "프로그램실행이력")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ProgramExecHistDto extends BaseDto {
    @Schema(description = "이력ID")
    private String histId;
    @Schema(description = "프로그램ID")
    private String pgmId;
    @Schema(description = "프로그램명")
    private String pgmNm;
    @Schema(description = "프로그램실행시작시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime execStrtDttm;
    @Schema(description = "프로그램실행종료시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime execEndDttm;
    @Schema(description = "실행유형코드", implementation = ExecutionType.class)
    private ExecutionType execTypeCd;
    @Schema(description = "실행상태코드", implementation = ExecStat.class)
    private ExecStat execSttsCd;
    @Schema(description = "프로세스ID")
    private String procsId;
    @Schema(description = "결과폴더ID")
    private String rsltDirId;
    @Schema(description = "결과파일용량")
    private Long rsltBytes;
    @Schema(description = "에러내용")
    private String errText;
    @ArraySchema(schema = @Schema(description = "결과파일정보"))
    private List<FileInfoDto> resultFiles = new ArrayList<>();

    public static List<Expression<?>> projectionFields(QProgramExecHist qProgramExecHist) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(qProgramExecHist),
                List.of(
                        qProgramExecHist.histId,
                        qProgramExecHist.pgmId,
                        qProgramExecHist.execStrtDttm,
                        qProgramExecHist.execEndDttm,
                        qProgramExecHist.execTypeCd,
                        qProgramExecHist.execSttsCd,
                        qProgramExecHist.procsId,
                        qProgramExecHist.rsltDirId,
                        qProgramExecHist.errText,
                        qProgramExecHist.rsltBytes
                )
        );
    }

    public static List<Expression<?>> projectionFields (QProgramExecHist qProgramExecHist, QProgram qProgram) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qProgramExecHist),
                List.of(
                        qProgram.pgmNm
                )
        );
    }
}
