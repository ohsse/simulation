package com.hscmt.simulation.program.comp;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramInputFileManager {
    private final VirtualEnvironmentComponent comp;

    public void deleteInputFiles (String pgmId, List<String> fileNames) {
        /* 지워야할 파일명 목록이 없다면 return */
        if (fileNames == null || fileNames.isEmpty()) return;
        /* 삭제해야할 대상 디렉토리 */
        String targetDirPath = FileUtil.getDirPath(comp.getProgramBasePath(), pgmId);
        /* 디렉토리 내 파일목록 가져오기 */
        List<File> files = FileUtil.getOnlyFilesInDirectory(targetDirPath);
        /* 파일순회하면서 연관파일 있다면 삭제 */
        if (files!= null && !files.isEmpty()) {
            files.stream()
                    .filter(file -> fileNames.contains(file.getName()))
                    .forEach(file -> {
                        try {
                            FileUtil.retryDelete(file.getPath());
                        } catch (Exception e) {
                            log.error(e.getMessage());
                            throw new RestApiException(FileErrorCode.FILE_DELETE_ERROR);
                        }
                    });
        }
    }
}
