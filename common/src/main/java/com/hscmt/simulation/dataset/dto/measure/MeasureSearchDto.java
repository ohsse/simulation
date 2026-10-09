package com.hscmt.simulation.dataset.dto.measure;

import com.hscmt.common.dto.FromToSearchDto;
import com.hscmt.common.enumeration.CycleCd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "계측데이터조회")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class MeasureSearchDto extends FromToSearchDto {
    @Schema(description = "계측간격", implementation = CycleCd.class)
    private CycleCd cyclCd;
}
