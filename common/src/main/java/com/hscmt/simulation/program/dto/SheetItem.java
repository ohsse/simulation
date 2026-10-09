package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "엑셀|csv시트정보")
@Data
@NoArgsConstructor
public class SheetItem {
    private String sheetName;
    private List<String> headers;
}
