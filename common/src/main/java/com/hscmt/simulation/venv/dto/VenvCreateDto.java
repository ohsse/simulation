package com.hscmt.simulation.venv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "가상환경 생성")
public class VenvCreateDto {
    @Schema(description = "가상환경명")
    private String venvNm;
    @Schema(description = "가상환경설명")
    private String venvDesc;
    @Schema(description = "파이썬버전")
    private String pyVrsn;
    @Schema(description = "라이브러리_ID_목록")
    private List<String> lbrIds = new ArrayList<>();
}
