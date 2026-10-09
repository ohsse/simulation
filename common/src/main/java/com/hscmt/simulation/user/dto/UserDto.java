package com.hscmt.simulation.user.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.enumeration.AuthCd;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.user.domain.QUser;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "사용자 정보")
public class UserDto extends BaseDto {
    @Schema(description = "사용자_ID")
    private String userId;
    @Schema(description = "사용자_명")
    private String userNm;
    @Schema(description = "사용자_비밀번호")
    private String userPwd;
    @Schema(description = "사용자_email")
    private String userEmail;
    @Schema(description = "사용자_전화번호")
    private String userTelno;
    @Schema(description = "사용자_휴대전화번호")
    private String userMblno;
    @Schema(description = "권한코드", implementation = AuthCd.class)
    private AuthCd authCd;

    public static List<Expression<?>> projectionFields(QUser qUser) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qUser.userId,
                        qUser.userNm,
                        qUser.userEmail,
                        qUser.userTelno,
                        qUser.userMblno,
                        qUser.authCd
                ), BaseDto.getBaseFields(qUser)
        );
    }
}


