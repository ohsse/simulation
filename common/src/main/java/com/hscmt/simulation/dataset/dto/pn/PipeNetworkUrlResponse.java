package com.hscmt.simulation.dataset.dto.pn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Schema(description = "tiff 응답")
@Data
public class PipeNetworkUrlResponse implements PipeNetworkVisResponse {
    @Schema(description = "응답유형")
    private String type = "url";
    @Schema(description = "대상경로")
    private String targetUrl;

    public PipeNetworkUrlResponse(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
