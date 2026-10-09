package com.hscmt.simulation.venv.comp;

import com.hscmt.common.enumeration.CudCodes;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.response.CommandResult;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.ProcessUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.venv.dto.VenvPackageDto;
import com.hscmt.simulation.venv.dto.VenvUpsertResultDto;
import com.hscmt.simulation.venv.error.VenvErrorCode;
import com.hscmt.simulation.venv.service.VenvHookService;
import com.hscmt.simulation.venv.service.VenvWatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class VenvManager {
    private final VenvHookService hookService;
    private final VenvWatcherService watcherService;
    private final VirtualEnvironmentComponent comp;
    private final VenvLibraryManager venvLibraryManager;
    private final String PACKAGE_FILES_DIR = FileUtil.getDirPath("Lib", "site-packages");

    /* 가상환경 생성 */
    public void createVenv (String venvId, String pyVrsn, List<String> lbrIds ) {
        /* 가상환경 설치 경로 */
        String venvPath = FileUtil.getDirPath(comp.getVenvBasePath(), venvId);
        /* 복사할 가상환경 경로 */
        String targetEnv = FileUtil.getDirPath(comp.getAnacondaPythonEnvPath(), pyVrsn);
        /* 가상환경 설치경로 생성 */
        boolean isCreate = FileUtil.createFile(venvPath);
        /* 폴더생성이 성공했다면 가상환경 생성을 시작한다. */
        if (isCreate) {
            List<String> failedList;
            /* 가상환경 생성 커맨드 만들기 */
            StringBuffer createCommand = new StringBuffer(comp.getAnacondaActivatePath())
                    .append(" && ")
                    .append("conda create --prefix ")
                    .append(venvPath)
                    .append(" --clone ")
                    .append(targetEnv)
                    .append(" --y");

            VenvUpsertResultDto dto = new VenvUpsertResultDto();
            dto.setVenvId(venvId);
            try {
                /* 가성환경 생성 시작 */
                CommandResult cmdResult  = ProcessUtil.runCommand(createCommand.toString());
                int exitCode = cmdResult.getExitCode();

                if (exitCode == 0) {
                    log.info(cmdResult.getOutputMessage());
                    log.info("가상환경 생성 성공 : {}", venvPath);
                    hookService.createComplete(venvId);
                    failedList = venvLibraryManager.installVenvPackages(venvPath, lbrIds);
                    if (failedList.size() == 0) {
                        /* 가상환경 설치 후, 패키지 설치까지 완료 시킨 다음 status 값 전송 */
                        watcherService.notifyStatus(venvId, dto, CudCodes.CREATED.name());
                    } else {
                        dto.setUploadFailedPackages(failedList);
                        /* 가상환경 설치 후, 패키지 설치까지 완료 시킨 다음 status 값 전송 */
                        watcherService.notifyError(venvId, dto, VenvErrorCode.PACKAGE_INSTALL_FAILED);
                    }
                } else {
                    log.info(cmdResult.getErrorMessage());
                    log.error("가상환경 생성 실패 : exitCode = {}, createCommand = {}", exitCode, createCommand.toString());
                    watcherService.notifyStatus(venvId, dto, CudCodes.FAILED.name());
                }
            } catch (Exception e) {
                log.error("create virtual environment error : {}", e.getMessage());
                watcherService.notifyError(venvId, e);
            }
        }
    }

    public void updateVenv (String venvId, List<String> addLbrIds, List<String> delLbrNms) {
        List<String> failedList = new ArrayList<>();

        VenvUpsertResultDto dto = new VenvUpsertResultDto();
        dto.setVenvId(venvId);
        String venvPath = FileUtil.getDirPath(comp.getVenvBasePath(), venvId);
        try {
            /* 삭제 패키지 존재한다면 삭제 */
            if (delLbrNms.size() > 0) {
                venvLibraryManager.deleteVenvPackages(venvPath, delLbrNms);
            }

            /* 추가 패키지 존재한다면 추가 */
            if (addLbrIds.size() > 0) {
                failedList = venvLibraryManager.installVenvPackages(venvPath, addLbrIds);
            }
            if (failedList.size() == 0) {
                watcherService.notifyStatus(venvId, dto, CudCodes.UPDATED.name());
            } else {
                dto.setUploadFailedPackages(failedList);
                watcherService.notifyError(venvId, dto, VenvErrorCode.PACKAGE_INSTALL_FAILED);
            }
        }  catch (Exception e) {
            log.error("update virtual environment system error : {}", e.getMessage());
            watcherService.notifyError(venvId, e);
        }
    }

    public void deleteVenv (String venvId) {
        String venvPath = FileUtil.getDirPath(comp.getVenvBasePath(), venvId);
        StringBuffer deleteCommand = new StringBuffer(comp.getAnacondaActivatePath())
                .append(" && ")
                .append("conda env remove --prefix ")
                .append(venvPath)
                .append(" --y");

        VenvUpsertResultDto dto = new VenvUpsertResultDto();
        dto.setVenvId(venvId);

        try {
            /* 가상환경 삭제 시작 */
            int exitCode = ProcessUtil.runCommand(deleteCommand.toString()).getExitCode();

            if (exitCode == 0) {
                log.info("가상환경 삭제 성공 : {}", venvPath);
                watcherService.notifyStatus(venvId, dto, CudCodes.DELETED.name());
            } else {
                log.error("가상환경 삭제 실패 : exitCode = {}, deleteCommand = {}", exitCode, deleteCommand.toString());
                watcherService.notifyError(venvId, dto, VenvErrorCode.ENV_DELETE_FAILED);
            }
        } catch (Exception e) {
            log.error("delete virtual environment error : {}", e.getMessage());
            watcherService.notifyError(venvId, e);
        }
    }

    /* 특정 가상환경에 라이브러리 폴더 내용물 조회 */
    public VenvPackageDto findAllRealFilesInVenvPackageDirectory (String venvId) {
        Path rootPath = Paths.get(FileUtil.getDirPath(comp.getVenvBasePath(), venvId, PACKAGE_FILES_DIR));

        Map<Path, List<Path>> groupedPaths = Map.of();

        try (Stream<Path> paths = Files.walk(rootPath)) {
            groupedPaths = paths.collect(Collectors.groupingBy(Path::getParent));
        } catch (IOException e) {
            throw new RestApiException(FileErrorCode.FILE_EXPLORER_ERROR);
        }

        return buildVenvPackageDto(rootPath, rootPath, groupedPaths);
    }

    /* 패키지 노드로 변환 */
    private VenvPackageDto buildVenvPackageDto (Path rootPath, Path path, Map<Path, List<Path>> groupedPaths) {
        final String rootName = "ROOT";
        boolean isDir = Files.isDirectory(path);

        VenvPackageDto dto = new VenvPackageDto();
        dto.setIsDir(isDir);
        dto.setFilePath(path.toFile().getAbsolutePath().replace(rootPath.toFile().getAbsolutePath(), rootName));
        if (path == rootPath){
            dto.setOrtxFileNm(rootName);
            dto.setParentPath(null);
        } else {
            dto.setOrtxFileNm(path.toFile().getName());
            dto.setParentPath(path.getParent().toFile().getAbsolutePath().replace(rootPath.toFile().getAbsolutePath(), rootName));
        }
        dto.setChildren(new ArrayList<>());

        if (isDir) {
            List<Path> children = groupedPaths.getOrDefault(path, new ArrayList<>());
            for (Path child : children) {
                dto.getChildren().add(buildVenvPackageDto(rootPath, child, groupedPaths));
            }
        }
        return dto;
    }

    /* 파일업로드 in 파이썬패키지 폴더 */
    public void uploadFilesInPackageDirectory (String venvId, String targetPath, List<MultipartFile> files) {
        String uploadDir = FileUtil.getDirPath(comp.getVenvBasePath(),venvId, PACKAGE_FILES_DIR, targetPath.replace("ROOT", ""));
        for (MultipartFile file : files) {
            try {
                FileUtil.uploadMultiPartFile(file, uploadDir);
            } catch (IOException e) {
                throw new RestApiException(FileErrorCode.FILE_UPLOAD_ERROR);
            }
        }
    }

    public void deletePackageFile (String venvId, String filePath) {
        FileUtil.removeFile(FileUtil.getFilePath(filePath.replace("ROOT" ,""), FileUtil.getDirPath(comp.getVenvBasePath(),venvId, PACKAGE_FILES_DIR)));
    }
}
