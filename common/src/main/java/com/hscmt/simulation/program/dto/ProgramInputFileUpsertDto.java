package com.hscmt.simulation.program.dto;

import com.hscmt.common.enumeration.InputFileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "프로그램입력파일 추가|수정")
@Data
@NoArgsConstructor
public class ProgramInputFileUpsertDto implements Serializable {
    @Schema(description = "프로그램입력파일ID")
    private String inputFileId;
    @Schema(description = "프로그램ID")
    private String pgmId;
    @Schema(description = "대상ID")
    private String trgtId;
    @Schema(description = "대상유형코드", implementation = InputFileType.class)
    private InputFileType trgtType;
}
