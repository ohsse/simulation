package com.hscmt.simulation.program.comp;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.simulation.program.dto.ProgramUpsertDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ProgramValidator {
    /* 신규 프로그램 검증 */
    public void validateNewProgram (ProgramUpsertDto dto, MultipartFile file) {
        /* 신규 프로그램이 파일 없을 시 에러 */
        checkFile(file);
    }

    public void checkFile (MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RestApiException(FileErrorCode.FILE_NOT_FOUND);
        }
    }
//    public void checkResultSet (List<ProgramResultUpsertDto> results) {
//        if (results == null || results.isEmpty()) {
//            throw new RestApiException(ProgramErrorCode.PROGRAM_RESULTSET_NOT_FOUND);
//        }
//    }
}
