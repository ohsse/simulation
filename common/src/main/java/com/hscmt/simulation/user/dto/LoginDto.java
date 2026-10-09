package com.hscmt.simulation.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginDto {
    @Schema(description = "ID", example = "sejinoh", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;
    @Schema(description = "password", example = "asdfij", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
