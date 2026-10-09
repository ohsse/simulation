package com.hscmt.simulation.common.aop;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.JwtTokenErrorCode;
import com.hscmt.common.jwt.enumeration.TokenState;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
@Slf4j
public class CheckJwtToken {
    private final JwtTokenProvider provider;

    @Around("execution(* com.hscmt.simulation..controller..*Controller.*(..)) "
            + "&& !@annotation(com.hscmt.simulation.common.annotation.UncheckedJwtToken) "
            + "&& !@within(com.hscmt.simulation.common.annotation.UncheckedJwtToken)")
    public <T> ResponseEntity<ResponseObject<T>> checkJwtToken (ProceedingJoinPoint joinPoint) throws Throwable {
        TokenState tokenState = provider.getTokenState();

        return switch (tokenState) {
            case VALID -> {
                Object result = joinPoint.proceed();

                if (result instanceof ResponseEntity<?> responseEntity) {
                    // 타입이 맞으면 안전하게 캐스팅
                    @SuppressWarnings("unchecked")
                    ResponseEntity<ResponseObject<T>> typedResponse =
                            (ResponseEntity<ResponseObject<T>>) responseEntity;
                    yield typedResponse;
                }
                // 타입이 예상과 다를 경우 방어적으로 예외 발생
                throw new IllegalStateException(
                        "Controller method must return ResponseEntity<ResponseObject<?>>, but got: "
                                + (result != null ? result.getClass() : "null")
                );
            }
            case INVALID -> throw new RestApiException(JwtTokenErrorCode.INVALID_TOKEN);
            case EXPIRED -> throw new RestApiException(JwtTokenErrorCode.EXPIRED_TOKEN);
        };
    }
}
