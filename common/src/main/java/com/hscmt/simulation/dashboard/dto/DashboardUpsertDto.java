package com.hscmt.simulation.dashboard.dto;

import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "대시보드 추가|수정")
@EqualsAndHashCode(callSuper = true)
public class DashboardUpsertDto extends GroupItemUpsertDto {
    @Schema(description = "대시보드_ID")
    private String dsbdId;
    @Schema(description = "대시보드명")
    private String dsbdNm;
    @Schema(description = "대시보드설명")
    private String dsbdDesc;
    @Schema(description = "해상도가로")
    private Integer resWidthVal;
    @Schema(description = "해상도세로")
    private Integer resHglnVal;
    @ArraySchema(schema = @Schema(implementation = DsbdVisItemDto.class, description = "대시보드시각화항목"))
    private List<DsbdVisItemDto> items = new ArrayList<>();
}
