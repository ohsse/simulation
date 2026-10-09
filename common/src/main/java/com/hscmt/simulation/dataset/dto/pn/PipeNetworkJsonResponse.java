package com.hscmt.simulation.dataset.dto.pn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

@Schema(description = "INP,SHP 응답")
@NoArgsConstructor
@Data
public class PipeNetworkJsonResponse implements PipeNetworkVisResponse{
    @Schema(description = "응답유형")
    private String type = "json";
    @Schema(description = "응답내용", implementation = JSONObject.class)
    private JSONObject content;

    public PipeNetworkJsonResponse(JSONObject content) {
        this.content = content;
    }
}
