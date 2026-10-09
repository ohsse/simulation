package com.hscmt.simulation.program.comp;

import com.github.f4b6a3.uuid.UuidCreator;
import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.ByteUnit;
import com.hscmt.common.enumeration.FileExtension;
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
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramFileManager {

    private final VirtualEnvironmentComponent vComp;

    /* 파일 리비전 경로에 파일 */
    public synchronized String uploadFile (String pgmId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        /* 이전 프로그램 파일 정리 */
        clearPrevProgramFile(pgmId);
        String saveDirPath;
        String pdirId;
        try {
            /* 프로그램 경로에 파일 저장 */
            String programDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId);
            FileUtil.uploadMultiPartFile(file, programDirPath);
            pdirId = UuidCreator.getTimeOrderedEpoch().toString();
            /* 파일이력 경로에 파일 저장 */
            saveDirPath = FileUtil.getDirPath(programDirPath, vComp.getREVISION_DIR(), pdirId);
            FileUtil.createFile(saveDirPath);
            FileUtil.copyFile(FileUtil.getFilePath(file.getOriginalFilename(), programDirPath), FileUtil.getFilePath(file.getOriginalFilename(), saveDirPath));
        } catch (IOException e) {
            log.error("file upload error : {} ", e.getMessage());
            throw new RestApiException(FileErrorCode.FILE_UPLOAD_ERROR);
        }

        return pdirId;
    }

    public void clearPrevProgramFile (String pgmId) {
        String lastFileDirPath = getLastUuidDirPath(FileUtil.getDirPath(vComp.getREVISION_DIR(), pgmId));
        if (lastFileDirPath != null) {
            String path = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.getREVISION_DIR(), lastFileDirPath);
            List<File> files =  FileUtil.getOnlyFilesInDirectory(path);
            Set<String> prevFileNames = new HashSet<>(files.stream().map(File::getName).toList());
            String programDir = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId);
            List<File> programFiles = FileUtil.getOnlyFilesInDirectory(programDir);
            programFiles.stream().filter(file -> prevFileNames.contains(file.getName())).forEach(File::delete);
        }
    }

    /* 프로그램 삭제시 프로그램 폴더 삭제 */
    public void deleteProgramDir (String pgmId) {
        String path = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId);
        try {
            FileUtil.retryDelete(path);
        } catch (Exception e) {
            log.error("file delete error : {} ", e.getMessage());
            throw new RestApiException(FileErrorCode.FILE_DELETE_ERROR);
        }
    }

    public String getProgramDirSize (String pgmId) {
        Path pgmDirPath = Paths.get(vComp.getProgramBasePath(), pgmId);
        long dirSize = FileUtil.getDirectorySize(pgmDirPath);
        return ByteUnit.humanize(dirSize);
    }

    /* 프로그램 개정과 프로그램 결과는 Uuid v7로 생성했으므로 Uuid 폴더명으로 마지막 생성 directory 가져오기 */
    public String getLastUuidDirPath (String dirPath) {
        Path historicPath = Paths.get(dirPath);

        if (!Files.exists(historicPath)) {
            return null;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(historicPath)) {
            String bestName = null;
            for (Path p : stream) {
                // 전제: 전부 디렉터리이자 v7 이름
                String name = p.getFileName().toString();
                if (bestName == null || name.compareTo(bestName) > 0) {
                    bestName = name;
                }
            }
            return bestName == null ? null : historicPath.resolve(bestName).getFileName().toString();
        } catch (IOException e) {
            log.error("file explorer error : {} ", e.getMessage());
            throw new RestApiException(FileErrorCode.FILE_EXPLORER_ERROR);
        }
    }

    /* 결과파일 삭제 */
    public void deleteAllResultFiles (String pgmId, String rsltNm, FileExtension fileXtns) {
        /* 프로그램 실행결과 이력경로 */
        String targetDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.getEXEC_RESULT_DIR());
        /* 삭제할 결과파일 명 만들기 */
        Set<String> deleteFileNames = fileXtns.getValidExtensions().stream().map(ext -> rsltNm + ext).collect(Collectors.toSet());
        /* 대상경로 */
        File targetDir = new File(targetDirPath);
        /* 이력경로가 폴더라면 */
        if (targetDir.isDirectory()) {
            /* 이력경로 내에 모든 파일 가져오기 */
            File[] files = targetDir.listFiles();
            /* 파일 순회 */
            for (File file : files) {
                /* 해당 파일이 폴더라면 */
                if (file.isDirectory()) {
                    String dirPath = file.getAbsolutePath();
                    /* 삭제파일 명 순회하면서 */
                    for (String deleteFileName : deleteFileNames) {
                        try {
                            /* 경로안에 있는 삭제 파일 삭제 */
                            Files.delete(Paths.get(FileUtil.getFilePath(deleteFileName, dirPath)));
                        } catch (IOException e) {
                            throw new  RestApiException(FileErrorCode.FILE_DELETE_ERROR);
                        }
                    }
                }
            }
        }
    }

    public FileInfoDto getLastProgramInfo (String pgmId) {

        String revisionDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.getREVISION_DIR());

        String lastDirId = getLastUuidDirPath(revisionDirPath);

        List<File> files = FileUtil.getOnlyFilesInDirectory(FileUtil.getDirPath(revisionDirPath, lastDirId));

        FileInfoDto fileInfoDto = new FileInfoDto();
        files.stream().findFirst().ifPresent(file -> {
            fileInfoDto.setFullFileName(file.getName());
            fileInfoDto.setFileExtension(FileUtil.getFileExtension(file.getName()));
            fileInfoDto.setFileNm(FileUtil.getFileNameWithoutExtension(file.getName()));
            fileInfoDto.setFileUrl(FileUtil.getUrlPath(file.getName(), vComp.getProgramBasePath().replace(vComp.getFileServerBasePath(),"") ,pgmId, vComp.REVISION_DIR, lastDirId));
        });

        return fileInfoDto;
    }

    public List<File> getProgramLastResultFiles (String pgmId) {
        String resultDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.EXEC_RESULT_DIR);
        String lastResultDir = getLastUuidDirPath(resultDirPath);
        return FileUtil.getOnlyFilesInDirectory(FileUtil.getDirPath(resultDirPath, lastResultDir));
    }

    public List<File> getProgramResultFiles (String pgmId, String dirId) {
        String resultDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.EXEC_RESULT_DIR);
        String targetDirPath = FileUtil.getDirPath(resultDirPath, dirId);
        return FileUtil.getOnlyFilesInDirectory(targetDirPath);
    }

    public String convertRequestUrlByFilePath (String filePath) {
        return FileUtil.convertFilePathToUrlPath(filePath, vComp.getFileServerBasePath());
    }

    public String getProgramResultDir (String pgmId) {
        return FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.EXEC_RESULT_DIR);
    }
}
