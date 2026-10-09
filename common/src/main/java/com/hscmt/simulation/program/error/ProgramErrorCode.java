package com.hscmt.simulation.program.error;

import com.hscmt.common.exception.error.ErrorCode;

public enum ProgramErrorCode implements ErrorCode {
    PROGRAM_NOT_FOUND,
    PROGRAM_RESULTSET_NOT_FOUND,
    PROGRAM_DATASET_NOT_FOUND,
    PROGRAM_RESULTSET_CONVERT_DISPLAY_FAILED,
    PROGRAM_RESULT_NOT_EXISTS,
    PROGRAM_VIS_SETUP_ERROR,
    NO_MORE_EXECUTION_HIST
}
