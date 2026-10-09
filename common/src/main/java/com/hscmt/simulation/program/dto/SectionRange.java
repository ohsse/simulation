package com.hscmt.simulation.program.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hscmt.common.enumeration.SectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
@Schema(description = "섹션범위정보")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SectionRange {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "섹션유형", example = "NODE", allowableValues = {"NODE", "LINK"})
    private String sectionType;
    @Schema(description = "속성ID", example = "velocity")
    private String attributeId;
    @Schema(description = "속석명", example = "유속", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String attributeNm;
    @Schema(description = "범위1", example = "0")
    private Double rngOdn1;
    @Schema(description = "범위2", example = "25")
    private Double rngOdn2;
    @Schema(description = "범위3", example = "50")
    private Double rngOdn3;
    @Schema(description = "범위4", example = "75")
    private Double rngOdn4;
    @Schema(description = "색상1", example = "#000000")
    private String colorOdn1;
    @Schema(description = "색상3", example = "#fcdedf")
    private String colorOdn3;
    @Schema(description = "색상2", example = "#abcdef")
    private String colorOdn2;
    @Schema(description = "색상4", example = "#ccdd11")
    private String colorOdn4;

    public void setAttributeName() {
        if (sectionType.equals("NODE")) {
            switch (attributeId) {
                case "demand" -> this.attributeNm = "용수수요량";
                case "head" -> this.attributeNm = "수두";
                case "pressure" -> this.attributeNm = "압력";
                case "quality" -> this.attributeNm = "수질";
            }
        } else if (sectionType.equals("LINK")) {
            switch (attributeId) {
                case "flow"-> this.attributeNm = "유량";
                case "velocity" -> this.attributeNm = "유속";
                case "headloss" -> this.attributeNm = "손실수두";
                case "quality" -> this.attributeNm = "수질";
            }
        }
    }
}
