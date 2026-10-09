package com.hscmt.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "파일정보")
@Data
public class FileInfoDto {
    @Schema(description = "파일명")
    private String fileNm;
    @Schema(description = "파일확장자")
    private String fileExtension;
    @Schema(description = "파일전체이름")
    private String fullFileName;
    @Schema(description = "파일URL")
    private String fileUrl;
    @Schema(description = "파일디렉토리ID")
    private String fileDirId;
}
