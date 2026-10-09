package com.hscmt.simulation.venv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "가상환경 정보 수정")
public class VenvUpdateDto {
    @Schema(description = "가상환경_ID")
    private String venvId;
    @Schema(description = "가상환경명")
    private String venvNm;
    @Schema(description = "가상환경설명")
    private String venvDesc;
    @Schema(description = "추가_라이브러리_ID_목록")
    private List<String> addLbrIds = new ArrayList<>();
    @Schema(description = "삭제_라이브러리명_목록")
    private List<String> delLbrNms = new ArrayList<>();
}
