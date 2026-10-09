package com.hscmt.simulation.user.domain;

import com.hscmt.common.domain.BaseEntity;
import com.hscmt.common.enumeration.AuthCd;
import com.hscmt.common.util.CryptoUtil;
import com.hscmt.simulation.user.dto.SignupDto;
import com.hscmt.simulation.user.dto.UserUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "user_m")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements Persistable<String> {
    /* 사용자 ID */
    @Id
    @Column(name = "user_id")
    private String userId;

    /* 사용자 명 */
    @Column(name = "user_nm")
    private String userNm;

    /* 사용자 비밀번호 */
    @Column(name = "user_pwd")
    private String userPwd;

    /* 사용자 이메일 */
    @Column(name = "user_email")
    private String userEmail;

    /* 사용자 휴대폰번호 */
    @Column(name = "user_mblno")
    private String userMblno;

    /* 사용자 전화번호 */
    @Column(name = "user_telno")
    private String userTelno;

    /* 권한코드 */
    @Column(name = "auth_cd")
    @Enumerated(EnumType.STRING)
    private AuthCd authCd;

    /* salt key */
    @Column(name = "salt_key")
    private String saltKey;

    /* 리프레시 토큰 */
    @Column(name = "rftk_val")
    private String rftkVal;

    /* 사용자 생성 */
    public User (SignupDto signupDto) {
        this.saltKey = CryptoUtil.createSalt();
        this.userId = signupDto.getUserId();
        this.userPwd = getNewPassword(signupDto.getUserPwd());
        this.userNm = signupDto.getUserNm();
        this.userEmail = signupDto.getUserEmail();
        this.userMblno = signupDto.getUserMblno();
        this.userTelno = signupDto.getUserTelno();

        if (signupDto instanceof UserUpsertDto upsertDto) {
            this.authCd = upsertDto.getAuthCd();
        } else {
            this.authCd = AuthCd.NORMAL;
        }
    }

    /* 사용자 수정 */
    public void updateInfo (UserUpsertDto userUpsertDto) {
        if (userUpsertDto.getUserNm() != null) {
            this.userNm = userUpsertDto.getUserNm().isEmpty() ? null : userUpsertDto.getUserNm();
        }
        if (userUpsertDto.getUserEmail() != null) {
            this.userEmail = userUpsertDto.getUserEmail().isEmpty() ? null : userUpsertDto.getUserEmail();
        }
        if (userUpsertDto.getUserMblno() != null) {
            this.userMblno = userUpsertDto.getUserMblno().isEmpty() ? null : userUpsertDto.getUserMblno();
        }
        if (userUpsertDto.getUserTelno() != null) {
            this.userTelno = userUpsertDto.getUserTelno().isEmpty() ? null : userUpsertDto.getUserTelno();
        }
        if (userUpsertDto.getAuthCd() != null) {
            this.authCd = userUpsertDto.getAuthCd();
        }
        if (userUpsertDto.getUserPwdNew() != null && !userUpsertDto.getUserPwdNew().isBlank()) {
            changePassword(userUpsertDto.getUserPwdNew());
        }
    }

    /* 비밀번호 변경 */
    public void changePassword(String newPassword) {
        this.userPwd = getNewPassword(newPassword);
    }

    public void changeRefreshToken(String refreshToken) {
        this.rftkVal = refreshToken;
    }

    protected String getNewPassword (String newUserPwd) {
        return CryptoUtil.encryptSHA256(newUserPwd, this.saltKey);
    }

    @Override
    public String getId() {
        return this.userId;
    }

    @Override
    public boolean isNew() {
        return this.getRgstDttm() == null;
    }
}
