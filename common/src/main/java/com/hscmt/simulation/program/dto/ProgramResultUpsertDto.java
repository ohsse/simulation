package com.hscmt.simulation.program.dto;

import com.hscmt.common.enumeration.FileExtension;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "프로그램 결과 정보")
public class ProgramResultUpsertDto {
    @Schema(description = "프로그램결과_ID")
    private String rsltId;
    @Schema(description = "프로그램ID")
    private String pgmId;
    @Schema(description = "결과명")
    private String rsltNm;
    @Schema(description = "파일확장자", implementation = FileExtension.class)
    private FileExtension fileXtns;
}
