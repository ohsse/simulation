package com.hscmt.simulation.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupDto {
    @Schema(description = "사용자_ID", example = "sejinoh", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
    @Schema(description = "사용자_명", example = "오세진", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userNm;
    @Schema(description = "사용자_비밀번호", example = "dhtp0714", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userPwd;
    @Schema(description = "사용자_email")
    private String userEmail;
    @Schema(description = "사용자_전화번호")
    private String userTelno;
    @Schema(description = "사용자_휴대전화번호")
    private String userMblno;
}
