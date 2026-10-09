package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "차트|그리드아이템항목")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ExcelAndCsvItemDto extends ProgramVisualizationItemDto{
    @ArraySchema(schema = @Schema(implementation = SheetItem.class, description = "시트정보"))
    private List<SheetItem> itemList;
}
