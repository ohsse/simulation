package com.hscmt.common.exception.advice;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.ErrorCode;
import com.hscmt.common.exception.error.JwtTokenErrorCode;
import com.hscmt.common.exception.error.UserErrorCode;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.program.error.ProgramHistErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(basePackages = {"com.hscmt"})
public class RestApiAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RestApiException.class)
    public <T> ResponseEntity<ResponseObject<T>> handleRestApiException (final RestApiException e) {
        final ErrorCode errorCode = e.getErrorCode();
        if (errorCode instanceof JwtTokenErrorCode) {

            if (errorCode == JwtTokenErrorCode.REFRESH_CHECK_FAIL) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(makeErrorResponse(errorCode));
            } else {
                // 401 Unauthorized 반환
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(makeErrorResponse(errorCode));
            }


        } else if (errorCode instanceof ProgramHistErrorCode) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(makeErrorResponse(errorCode));
        } else {

            if (errorCode == UserErrorCode.ENABLE_USER_AUTH) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body(makeErrorResponse(errorCode));
            }

            // 400 Bad Request 반환
            return ResponseEntity.badRequest()
                    .body(makeErrorResponse(errorCode));
        }
    }

    protected <T> ResponseObject<T> makeErrorResponse (ErrorCode errorCode) {
        return ResponseObject.<T>builder()
                .code(errorCode.name())
                .build();
    }



    @ExceptionHandler(Exception.class)
    public <T> ResponseEntity<ResponseObject<T>> handleException (final Exception e) {
        log.error("internal Sever Error : {}", e.getMessage());
        return ResponseEntity.internalServerError()
                .body(makeErrorResponse(e));
    }

    protected <T> ResponseObject<T> makeErrorResponse (Exception e) {
        return ResponseObject.<T>builder()
                .code(e.getClass().getSimpleName())
                .build();
    }
}
