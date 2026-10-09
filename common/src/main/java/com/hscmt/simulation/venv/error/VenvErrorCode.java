package com.hscmt.simulation.venv.error;

import com.hscmt.common.exception.error.ErrorCode;


public enum VenvErrorCode implements ErrorCode {
    INVALID_PYTHON_VERSION,
    ENV_CREATE_FAILED,
    ENV_UPDATE_FAILED,
    ENV_DELETE_FAILED,
    PACKAGE_INSTALL_FAILED,
    PACKAGE_DELETE_FAILED,
    ENV_NOT_FOUND
    ;
}
