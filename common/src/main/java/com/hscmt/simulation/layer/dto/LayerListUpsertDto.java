package com.hscmt.simulation.layer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "레이어내역저장")
@NoArgsConstructor
@Data
public class LayerListUpsertDto {
    @Schema(description = "레이어ID")
    private String layerId;
    @Schema(description = "객체타입")
    private String ftype;
    @Schema(description = "객체ID")
    private Long fid;
    @Schema(description = "좌표정보")
    private String gmtrVal;
    @Schema(description = "속성정보")
    private String property;
    @Schema(description = "로그인ID")
    private String loginId;
    @Schema(description = "색상정보")
    private String colorStr;
}
