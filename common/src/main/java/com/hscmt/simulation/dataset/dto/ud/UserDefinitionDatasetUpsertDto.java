package com.hscmt.simulation.dataset.dto.ud;

import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "사용자정의 데이터셋 추가|수정")
@EqualsAndHashCode(callSuper = true)
public class UserDefinitionDatasetUpsertDto extends DatasetUpsertDto {
}
