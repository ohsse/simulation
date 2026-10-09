package com.hscmt.simulation.venv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "가상환경 추가|수정 결과")
public class VenvUpsertResultDto {
    @Schema(description = "가상환경_ID")
    private String venvId;
    @Schema(description = "등록실패_패키지목록")
    private List<String> uploadFailedPackages = new ArrayList<>();
    @Schema(description = "삭제실패_패키지목록")
    private List<String> deleteFailedPackages = new ArrayList<>();
}
