package com.hscmt.simulation.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "노드 결과 정보")
public class NodeResultDto {
    @Schema(description = "보고서시간")
    private String reportTime;
    @Schema(description = "노드ID")
    private String id;
    @Schema(description = "용수수요량")
    private String demand;
    @Schema(description = "용수수요량_색상")
    private String demandColor;
    @Schema(description = "총수두")
    private String head;
    @Schema(description = "총수두_색상")
    private String headColor;
    @Schema(description = "압력")
    private String pressure;
    @Schema(description = "압력_색상")
    private String pressureColor;
    @Schema(description = "수질")
    private String quality;
    @Schema(description = "수질_색상")
    private String qualityColor;

    @Builder
    public NodeResultDto (Map<String, Object> nodeInfo) {
        if (!nodeInfo.isEmpty()) {
            if (nodeInfo.containsKey("reportTime")) {
                this.reportTime = (String) nodeInfo.get("reportTime");
            }
            if (nodeInfo.containsKey("id")) {
                this.id = (String) nodeInfo.get("id");
            }
            if (nodeInfo.containsKey("demand")) {
                this.demand = (String) nodeInfo.get("demand");
            }
            if (nodeInfo.containsKey("demandColor")) {
                this.demandColor = (String) nodeInfo.get("demandColor");
            }
            if (nodeInfo.containsKey("head")) {
                this.head = (String) nodeInfo.get("head");
            }
            if (nodeInfo.containsKey("headColor")) {
                this.headColor = (String) nodeInfo.get("headColor");
            }
            if (nodeInfo.containsKey("pressure")) {
                this.pressure = (String) nodeInfo.get("pressure");
            }
            if (nodeInfo.containsKey("pressureColor")) {
                this.pressureColor = (String) nodeInfo.get("pressureColor");
            }
            if (nodeInfo.containsKey("quality")) {
                this.quality = (String) nodeInfo.get("quality");
            }
            if (nodeInfo.containsKey("qualityColor")) {
                this.qualityColor = (String) nodeInfo.get("qualityColor");
            }
        }
    }
}
