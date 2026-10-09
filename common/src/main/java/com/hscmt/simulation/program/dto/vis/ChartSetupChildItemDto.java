package com.hscmt.simulation.program.dto.vis;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Schema(name = "차트세부설정")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartSetupChildItemDto {
    @Schema(description = "항목ID")
    private String id;
    @Schema(description = "라벨")
    private String label;
    @Schema(description = "데이터Key", example = "result.xlsx|sheet1|0")
    private String dataKey;
    @ArraySchema(schema = @Schema(implementation = ChartSetupChildItemDto.class))
    private List<ChartSetupChildItemDto> children = new ArrayList<>();
    @Schema(description = "container")
    @JsonAlias({"isContainer"})
    private boolean container;
    @Schema(description = "방향")
    private String orientation;
    @Schema(description = "기준축ID")
    private String baseAxisId;
    @Schema(description = "차트유형", example = "line")
    private String type;
    @Schema(description = "색상값",example = "#000000")
    private String color;

    @JsonProperty("isContainer")
    public boolean isContainer() {return container;}

    @JsonProperty("isContainer")
    public void setContainer(boolean container) {this.container = container;}
}
