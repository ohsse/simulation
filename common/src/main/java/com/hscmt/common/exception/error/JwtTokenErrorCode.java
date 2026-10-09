package com.hscmt.common.exception.error;

public enum JwtTokenErrorCode implements ErrorCode{
    INVALID_TOKEN,
    EXPIRED_TOKEN,
    UNSUPPORTED_TOKEN,
    REFRESH_CHECK_FAIL,
    ;
}
