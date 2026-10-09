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
@Schema(description = "링크 결과 정보")
public class LinkResultDto {
    @Schema(description = "보고서시간")
    private String reportTime;
    @Schema(description = "링크ID", example = "A30")
    private String id;
    @Schema(description = "유량")
    private String flow;
    @Schema(description = "유량_색상")
    private String flowColor;
    @Schema(description = "유속")
    private String velocity;
    @Schema(description = "유속_색상")
    private String velocityColor;
    @Schema(description = "손실수두")
    private String headloss;
    @Schema(description = "손실수두_색상")
    private String headlossColor;
    @Schema(description = "수질")
    private String quality;
    @Schema(description = "수질_색상")
    private String qualityColor;
    @Schema(description = "상태")
    private String status;

    @Builder
    public LinkResultDto (Map<String, Object> linkInfo) {
        if (!linkInfo.isEmpty()) {
            if (linkInfo.containsKey("reportTime")) {
                this.reportTime = (String) linkInfo.get("reportTime");
            }
            if (linkInfo.containsKey("id")) {
                this.id = (String) linkInfo.get("id");
            }
            if (linkInfo.containsKey("flow")) {
                this.flow = (String) linkInfo.get("flow");
            }
            if (linkInfo.containsKey("flowColor")) {
                this.flowColor = (String) linkInfo.get("flowColor");
            }
            if (linkInfo.containsKey("velocity")) {
                this.velocity = (String) linkInfo.get("velocity");
            }
            if (linkInfo.containsKey("velocityColor")) {
                this.velocityColor = (String) linkInfo.get("velocityColor");
            }
            if (linkInfo.containsKey("headloss")) {
                this.headloss = (String) linkInfo.get("headloss");
            }
            if (linkInfo.containsKey("headlossColor")) {
                this.headlossColor = (String) linkInfo.get("headlossColor");
            }
            if (linkInfo.containsKey("quality")) {
                this.quality = (String) linkInfo.get("quality");
            }
            if (linkInfo.containsKey("qualityColor")) {
                this.qualityColor = (String) linkInfo.get("qualityColor");
            }
            if (linkInfo.containsKey("status")) {
                this.status = (String) linkInfo.get("status");
            }
        }
    }
}
