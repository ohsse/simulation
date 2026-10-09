package com.hscmt.common.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "Jwt Token 스펙")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
    @Schema(description = "접근확인용토큰")
    private String accessToken;
    @Schema(description = "재생성주기토큰")
    private String refreshToken;
}