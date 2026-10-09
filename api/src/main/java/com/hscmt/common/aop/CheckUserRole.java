package com.hscmt.common.aop;

import com.hscmt.common.enumeration.AuthCd;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.UserErrorCode;
import com.hscmt.simulation.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class CheckUserRole {

    private final JwtTokenProvider provider;

    @Around("@annotation(com.hscmt.common.aop.UserRoleCheckRequired)")
    public Object checkUserRole(ProceedingJoinPoint joinPoint) throws Throwable {
        String role = provider.getClaim(provider.getTokenStr(), "role");

        AuthCd authCd = AuthCd.valueOf(role);

        if (authCd == AuthCd.NORMAL) {
            throw new RestApiException(UserErrorCode.ENABLE_USER_AUTH);
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        UserRoleCheckRequired annotation = method.getAnnotation(UserRoleCheckRequired.class);

        AuthCd enableAuth = annotation.enableAuth();

        if (authCd == enableAuth) {
            throw new RestApiException(UserErrorCode.ENABLE_USER_AUTH);
        }

        return joinPoint.proceed();
    }
}
