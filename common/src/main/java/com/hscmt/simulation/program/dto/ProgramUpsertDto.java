package com.hscmt.simulation.program.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "프로그램 추가|수정")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramUpsertDto extends GroupItemUpsertDto {
    @Schema(description = "프로그램_ID")
    private String pgmId;
    @Schema(description = "프로그램명")
    private String pgmNm;
    @Schema(description = "프로그램설명")
    private String pgmDesc;
    @Schema(description = "가상환경_ID")
    private String venvId;
    @Schema(description = "실시간여부", implementation = YesOrNo.class)
    private YesOrNo rltmYn;
    @Schema(description = "실행반복주기", implementation = CycleCd.class)
    private CycleCd rpttIntvTypeCd;
    @Schema(description = "실행반복간격")
    private Integer rpttIntvVal;
    @Schema(description = "최초실행일시")
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm:ss") // 쿼리파라미터 폼
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // request body 나 응답
    private LocalDateTime strtExecDttm;
    @ArraySchema(schema = @Schema(description = "프로그램인수", implementation = ProgramArgDto.class))
    private List<ProgramArgDto> pgmArgs;
    @ArraySchema(schema = @Schema(description = "프로그램결과", implementation = ProgramResultUpsertDto.class))
    private List<ProgramResultUpsertDto> results;
    @ArraySchema(schema = @Schema(description = "프로그램입력파일", implementation = ProgramInputFileUpsertDto.class))
    private List<ProgramInputFileUpsertDto> inputFiles;
    @ArraySchema(schema = @Schema(description = "프로그램시각화", implementation = ProgramVisualizationUpsertDto.class))
    private List<ProgramVisualizationUpsertDto> visualizations;
}
