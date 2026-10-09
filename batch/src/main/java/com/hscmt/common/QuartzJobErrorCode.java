package com.hscmt.common;

import com.hscmt.common.exception.error.ErrorCode;

public enum QuartzJobErrorCode implements ErrorCode {
    DELETE_JOB_ERROR,
    CHECK_JOB_ERROR,
    CHECK_TRIGGER_ERROR,
    REGISTER_JOB_ERROR,
    UPDATE_JOB_ERROR,
    GROUPING_JOB_ERROR
    ;
}
