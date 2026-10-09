package com.hscmt.simulation.dataset.dto.measure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "계측데이터셋상세항목 추가")
public class MeasureDatasetDetailUpsertDto {
    @Schema(description = "태그번호")
    private String tagSn;
    @Schema(description = "태그설명")
    private String tagDesc;
    @Schema(description = "태그유형코드")
    private String tagSeCd;
    @Schema(description = "정렬순서")
    private Integer sortOrd;
}
