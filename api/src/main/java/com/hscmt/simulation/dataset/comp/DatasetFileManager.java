package com.hscmt.simulation.dataset.comp;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatasetFileManager {
    private final VirtualEnvironmentComponent comp;

    public String getDsDirId (String dsId) {
        return FileUtil.getDirPath(comp.getDatasetBasePath(), dsId);
    }

    /* 유효한 파일만 데이터셋 저장공간에 업로드 */
    public void uploadValidFiles (List<MultipartFile> files, Set<String> validExtensions, String dsId) {
        if (files == null || files.isEmpty()) return;
        String saveDirPath = FileUtil.getDirPath(comp.getDatasetBasePath(), dsId);

        try {
            FileUtil.deleteFilesInDirectory(saveDirPath);
        } catch (Exception e) {
            fileDeleteError(e);
        }

        files.stream()
                .filter(file -> validExtensions.contains(FileUtil.getFileExtension(file.getOriginalFilename())))
                .forEach(file -> uploadFile(file, saveDirPath));
    }

    public List<String> getDatasetFileNames (String dsId) {
        return FileUtil.getOnlyFilesInDirectory(FileUtil.getDirPath(comp.getDatasetBasePath(), dsId))
                .stream()
                .map(File::getName)
                .toList();
    }

    public List<File> getDatasetFiles (String dsId) {
        return FileUtil.getOnlyFilesInDirectory(FileUtil.getDirPath(comp.getDatasetBasePath(), dsId));
    }

    public String convertFileUrl (String dsId, String fileName) {
        return FileUtil.getUrlPath(fileName,comp.getDatasetBasePath().replace(comp.getFileServerBasePath(),""), dsId);
    }

    /* 파일업로드 */
    protected void uploadFile (MultipartFile file, String saveDirPath) {
        try {
            FileUtil.uploadMultiPartFile(file, saveDirPath);
        } catch (IOException e) {
            log.error("file upload error : {} ", e.getMessage());
            throw new RestApiException(FileErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /* 데이터셋 ID 폴더 삭제 */
    public void deleteDatasetDir (String dsId) {
        String path = FileUtil.getDirPath(comp.getDatasetBasePath(), dsId);
        try {
            FileUtil.retryDelete(path);
        } catch (Exception e) {
            fileDeleteError(e);
        }
    }

    public void fileDeleteError(Exception e) {
        log.error("file delete error : {} ", e.getMessage());
        throw new RestApiException(FileErrorCode.FILE_DELETE_ERROR);
    }
}
