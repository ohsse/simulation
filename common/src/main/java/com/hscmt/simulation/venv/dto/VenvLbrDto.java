package com.hscmt.simulation.venv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "가상환경 적용 파이썬 라이브러리 정보")
@Data
@NoArgsConstructor
public class VenvLbrDto {
    @Schema(description = "라이브러리명")
    private String lbrNm;
    @Schema(description = "라이브러리버전")
    private String lbrVrsn;
}
