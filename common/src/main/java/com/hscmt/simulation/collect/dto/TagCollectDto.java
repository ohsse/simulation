package com.hscmt.simulation.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Schema(description = "태그데이터 수집")
public class TagCollectDto implements Serializable {
    @Schema(description = "태그번호")
    private String tagsn;
    @Schema(description = "태그유형코드")
    private String tagSeCd;
    @Schema(description = "대상로그시간")
    private String targetLogTime;
    @Schema(description = "시작로그시간")
    private String startLogTime;
    @Schema(description = "종료로그시간")
    private String endLogTime;
}
