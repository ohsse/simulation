package com.hscmt.simulation.dataset.dto.pn;

import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "관망데이터셋 추가|수정")
public class PipeNetworkDatasetUpsertDto extends DatasetUpsertDto {
    @Schema(description = "좌표계유형", implementation = CrsyType.class)
    private CrsyType crsyTypeCd;
}
