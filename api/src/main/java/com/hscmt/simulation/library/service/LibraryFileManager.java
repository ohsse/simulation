package com.hscmt.simulation.library.service;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.PyPackageUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.library.domain.Library;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LibraryFileManager {

    private final VirtualEnvironmentComponent comp;

    /* 등록 가능한 휠 파일 추리기 */
    public List<MultipartFile> filterWheelFiles (List<MultipartFile> files) {
        List<MultipartFile> enableFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            for (String pythonVersion : comp.getENABLE_PYTHON_VERSIONS()) {
                String fileName = file.getOriginalFilename();
                if (PyPackageUtil.isAbleSelectedPythonVersion(fileName, pythonVersion)) {
                    enableFiles.add(file);
                    break;
                }
            }
        }

        if (enableFiles.isEmpty()) {
            throw new RestApiException(FileErrorCode.INVALID_WHEEL_FILE);
        }

        return enableFiles;
    }

    /* 파일 업로드 */
    public void uploadLibraries (List<MultipartFile> files) {
        for (MultipartFile file : files) {
            try {
                FileUtil.uploadMultiPartFile(file, FileUtil.getDirPath(comp.getLibraryBasePath()));
            } catch (IOException e) {
                throw new RestApiException(FileErrorCode.FILE_UPLOAD_ERROR);
            }
        }
    }

    /* 파일정보로 엔터티 정보 취득 */
    public List<Library> convertEntitiesByFile (List<MultipartFile> files) {
        List<File> installedLibraries = FileUtil.getOnlyFilesInDirectory (comp.getLibraryBasePath());

        Set<String> existLibraryNames = installedLibraries.stream().map(File::getName).collect(java.util.stream.Collectors.toSet());
        Set<String> requestLibraryNames = files.stream().map(MultipartFile::getOriginalFilename).collect(java.util.stream.Collectors.toSet());

        requestLibraryNames.removeAll(existLibraryNames);

        List<Library> libraries = new ArrayList<>();

        for (String ortxFileName : requestLibraryNames) {
            String lbrNm = PyPackageUtil.getPackageNameTag(ortxFileName);
            String lbrVrsn = PyPackageUtil.getPackageVersionTag(ortxFileName);
            String pyVrsn = PyPackageUtil.getPythonVersionTag(ortxFileName);
            libraries.add(new Library(lbrNm, lbrVrsn, pyVrsn, ortxFileName));
        }

        return libraries;
    }

    /* 라이브러리 파일 삭제 */
    public void deleteLibrary (String libraryFileName) {
        FileUtil.removeFile(FileUtil.getFilePath(libraryFileName, comp.getLibraryBasePath()));
    }

}
