package com.hscmt.simulation.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "리프레시토큰 재발급 요청")
public class RefreshRequestDto {
    @Schema(description = "리프레시토큰")
    private String refreshToken;
}
