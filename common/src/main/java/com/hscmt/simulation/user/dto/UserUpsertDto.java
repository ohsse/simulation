package com.hscmt.simulation.user.dto;

import com.hscmt.common.enumeration.AuthCd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "사용자 정보 수정")
@EqualsAndHashCode(callSuper = true)
public class UserUpsertDto extends SignupDto {
    @Schema(description = "권한코드", implementation = AuthCd.class)
    private AuthCd authCd;
    @Schema(description = "신규비밀번호")
    private String userPwdNew;
}
