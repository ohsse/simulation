package com.hscmt.simulation.dataset.dto.measure;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Schema(description = "계측데이터셋 트렌드 결과")
@Data
@NoArgsConstructor
public class MeasureDatasetTrendDto {
    @ArraySchema(schema = @Schema(implementation = MeasureDatasetDetailDto.class, description = "계측데이터상세항목정보"))
    List<MeasureDatasetDetailDto> targetItems;
    @Schema(description = "데이터")
    List<Map<String, Object>> data;
}
