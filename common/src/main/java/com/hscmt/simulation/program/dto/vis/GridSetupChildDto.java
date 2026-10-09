package com.hscmt.simulation.program.dto.vis;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "그리드항목설정자식정보")
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GridSetupChildDto {
    @Schema(description = "항목ID")
    private String id;
    @Schema(description = "라벨")
    private String label;
    @Schema(description = "데이터키", example = "result.xlsx|sheet1|0")
    private String dataKey;
    @ArraySchema(schema = @Schema(implementation = GridSetupChildDto.class))
    private List<GridSetupChildDto> children;
    @Schema(description = "container")
    @JsonAlias({"isContainer"})
    private boolean container;

    @JsonProperty("isContainer")
    public boolean isContainer() {return container;}

    @JsonProperty("isContainer")
    public void setContainer(boolean container) {this.container = container;}
}
