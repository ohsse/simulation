package com.hscmt.simulation.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hscmt.common.jwt.enumeration.TokenState;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final SecretKey SECRET_KEY;

    // 서명 키는 환경변수 JWT_SECRET 으로 주입한다 (기본값은 로컬 개발 전용, api 모듈과 같은 값을 써야 한다)
    public JwtTokenProvider(@Value("${JWT_SECRET:local-dev-only-secret-change-me-0123456789}") String JWT_SECRET_STR) {
        this.SECRET_KEY = new SecretKeySpec(
                JWT_SECRET_STR.getBytes(StandardCharsets.UTF_8)
                , Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    /**
     * 액세스 토큰 발급
     * @param subject : 사용자 ID
     * @return accessToken
     */
    public String generateAccessToken(String subject) {
        return generateToken(subject, null, 1);
    }

    /**
     * 액세스 토큰 발급
     * @param subject : userId
     * @param additionalClaims : 추가할 클레임 정보 <key,value/>
     * @return accessToken
     */
    public String generateAccessToken(String subject, Map<String, Object> additionalClaims) {
        return generateToken(subject, additionalClaims, 1);
    }

    /**
     * 리프레시 토큰 발급
     * @param subject : userId
     * @return refreshToken
     */
    public String generateRefreshToken(String subject) {
        return generateToken(subject, null, 7);
    }

    /**
     * 리프레시 토큰 발급
     * @param subject : userId
     * @param additionalClaims : 추가할 클레임 <key,value/>
     * @return 리프레시 토큰
     */
    public String generateRefreshToken(String subject, Map<String, Object> additionalClaims) {
        return generateToken(subject, additionalClaims, 7);
    }

    /**
     * 토큰발급
     * @param subject : userId
     * @param additionalClaims : 추가할 클레임
     * @param expiresDays : 유효기간 [일]
     * @return 토큰
     */
    public String generateToken(String subject, Map<String, Object> additionalClaims, Integer expiresDays) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(LocalDateTime.now().plus(expiresDays, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .signWith(SECRET_KEY)
                .compact();
    }


    /**
     * 토큰발급
     * @param subject : userId
     * @param additionalClaims : 추가할 클레임
     * @param expireValue : 유효기간
     * @param chronoUnit : 유기간 단위
     * @return 토큰
     */
    public String generateToken(String subject, Map<String, Object> additionalClaims, Integer expireValue, ChronoUnit chronoUnit) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(LocalDateTime.now().plus(expireValue, chronoUnit).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .signWith(SECRET_KEY)
                .compact();
    }
    

    /**
     * subject 값 가져오기
     * @param token : Authorization token
     * @return : subject 값
     */
    public String getSubject (String token) {
        return getClaim(token, "sub");
    }

    /**
     * subject 값 가져오기
     * @return subject
     */
    public String getSubject () {
        return getSubject(getTokenStr());
    }

    public String getTokenStr () {
        return getTokenStr("Authorization");
    }

    public String getTokenStr (String tokenHeaderKey) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader(tokenHeaderKey);
    }

    /**
     * Claim 값 가져오기
     * @param token : Authorization token
     * @param name : claim key
     * @return : claim 값
     */
    public String getClaim (String token, String name) {
        if (token == null || token.isEmpty()) return null;
        DecodedJWT decodedJWT = JWT.decode(token);
        Claim claim = decodedJWT.getClaim(name);
        return claim.isNull() ? null : claim.asString().replace("\"", "");
    }

    /**
     * token state 값 가져오기
     * @param token : Authorization token
     * @return VALID | EXPIRED | INVALID
     */
    public TokenState getTokenState(String token) {
        if (token == null) {
            return TokenState.INVALID;
        }
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return TokenState.VALID;
        } catch (ExpiredJwtException e) {
            return TokenState.EXPIRED;
        } catch (Exception e) {
            return TokenState.INVALID;
        }
    }

    public TokenState getTokenState() {
        return getTokenState(getTokenStr());
    }
}
