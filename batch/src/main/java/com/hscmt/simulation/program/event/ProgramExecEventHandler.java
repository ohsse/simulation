package com.hscmt.simulation.program.event;

import com.github.f4b6a3.uuid.UuidCreator;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.layer.domain.Layer;
import com.hscmt.simulation.layer.dto.LayerUpsertRequest;
import com.hscmt.simulation.layer.repository.LayerRepository;
import com.hscmt.simulation.layer.service.LayerManageService;
import com.hscmt.simulation.program.dto.ProgramResultDto;
import com.hscmt.simulation.program.repository.ProgramRepository;
import com.hscmt.simulation.program.service.ProgramExecHistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramExecEventHandler {
    private final ProgramExecHistService histService;
    private final ProgramRepository programRepository;
    private final VirtualEnvironmentComponent vComp;
    private final LayerRepository layerRepository;
    private final LayerManageService layerManageService;

    @Async("batchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent (ProgramExecDeletedEvent event) {
        String dirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), event.pgmId(), vComp.getEXEC_RESULT_DIR(), event.rsltDirId());
        FileUtil.retryDelete(dirPath);
    }

    @Async("batchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent (ProgramExecSuccessEvent event) {
        String pgmId = event.pgmId();

        List<ProgramResultDto> resultList = programRepository.findProgramResultsByPgmId(pgmId);

        List<String> fileNames = new ArrayList<>();

        Map<String, List<String>> shapeFileMap = new HashMap<>();

        for (ProgramResultDto result : resultList) {
            FileExtension fileExtension = result.getFileXtns();
            List<String> fileExtensions = fileExtension == FileExtension.SHP ?
                    fileExtension.getRequiredExtensions() : fileExtension.getValidExtensions();

            for (String fileXtn : fileExtensions) {
                fileNames.add(result.getRsltNm() + fileXtn);
                if (result.getFileXtns() == FileExtension.SHP) {
                    shapeFileMap.computeIfAbsent(result.getRsltId(), k -> new ArrayList<>());
                    shapeFileMap.get(result.getRsltId()).add(result.getRsltNm() + fileXtn);
                }
            }
        }

        String programDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId);
        String resultDirBasePath = FileUtil.getDirPath(programDirPath, vComp.EXEC_RESULT_DIR);
        String resultDirId = UuidCreator.getTimeOrderedEpoch().toString();
        String saveDirPath = FileUtil.getDirPath(resultDirBasePath, resultDirId);

        for (String fileName : fileNames) {

            File resultFile = new File(FileUtil.getFilePath(fileName, programDirPath));

            if (!resultFile.exists()) {

                histService.failProgram(event.histId(), fileName + " 파일이 존재하지 않습니다.\n프로그램결과파일 설정을 다시 한번 확인해 주시기 바랍니다.");
                return;
            }

            try {
                FileUtil.copyFile(FileUtil.getFilePath(fileName, programDirPath), FileUtil.getFilePath(fileName, saveDirPath));
            } catch (NoSuchFileException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        /* 경로 변경 */
        if (!fileNames.isEmpty()) {
            histService.changeResultDir(event.histId(), resultDirId);
        }


        /* shp 결과파일 존재 시 */
        if (!shapeFileMap.isEmpty()){
            try {
                /* 레이어파일 경로로 결과 shp 파일 이동 */
                for (Map.Entry<String, List<String>> entry : shapeFileMap.entrySet()) {
                    String layerId = entry.getKey();
                    String layerDirPath = FileUtil.getDirPath(vComp.getLayerBasePath(), layerId);
                    entry.getValue().forEach(fileName -> {
                        try {
                            FileUtil.copyFile(FileUtil.getFilePath(fileName, programDirPath), FileUtil.getFilePath(fileName,layerDirPath));
                        } catch (NoSuchFileException e) {
                            log.error(e.getMessage());
                            throw new RestApiException(FileErrorCode.FILE_UPLOAD_ERROR);
                        }
                    });

                    /* 레이어 기본정보 검색 */
                    Layer findLayer = layerRepository.findLayer(layerId);

                    if (findLayer != null) {
                        /* 레이어 정보 존재시 바뀐 결과 파일로 레이어 재생성 */
                        layerManageService.migrateShpToDb(new LayerUpsertRequest(
                                findLayer.getLayerId(), findLayer.getCrsyTypeCd(), findLayer.getLayerStyles(), findLayer.getMdfId()
                        ));
                    }

                }
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RestApiException(FileErrorCode.FILE_UPLOAD_ERROR);
            }
        }
    }
}
