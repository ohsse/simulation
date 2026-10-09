package com.hscmt.simulation.program.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.enumeration.*;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.util.CsvUtil;
import com.hscmt.common.util.ExcelUtil;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dashboard.service.DashboardService;
import com.hscmt.simulation.program.comp.ProgramFileManager;
import com.hscmt.simulation.program.domain.ProgramResult;
import com.hscmt.simulation.program.domain.ProgramVisualization;
import com.hscmt.simulation.program.dto.*;
import com.hscmt.simulation.program.dto.vis.VisSetupItem;
import com.hscmt.simulation.program.dto.vis.VisualizationItem;
import com.hscmt.simulation.program.error.ProgramErrorCode;
import com.hscmt.simulation.program.error.ProgramHistErrorCode;
import com.hscmt.simulation.program.repository.ProgramExecHistRepository;
import com.hscmt.simulation.program.repository.ProgramRepository;
import com.hscmt.simulation.program.repository.ProgramResultRepository;
import com.hscmt.simulation.program.repository.ProgramVisualizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
@Slf4j
public class ProgramVisualizationService {
    private final ProgramVisualizationRepository programVisualizationRepository;
    private final ProgramResultRepository programResultRepository;
    private final ProgramExecHistRepository execHistRepository;
    private final ProgramFileManager fileManager;
    private final ProgramVisDataProcessor processor;
    private final DashboardService dashboardService;
    private final ProgramInputFileService programInputFileService;
    private final ProgramRepository programRepository;

    @SimulationTx
    public void save(String pgmId, List<ProgramVisualizationUpsertDto> dtoList) {
        if (dtoList != null) {
            /* 프로그램이 가지고 있는 모든 시각화 정보 목록을 조회 */
            List<ProgramVisualization> pgmVisualizations = programVisualizationRepository.findAllByPgmId(pgmId);

            /* 기존 ID */
            Set<String> existIds = new HashSet<>(pgmVisualizations.stream().map(programVisualization -> programVisualization.getVisId()).toList());
            /* 유지시킬 ID */
            Set<String> maintainIds = dtoList.stream()
                    .map(ProgramVisualizationUpsertDto::getVisId)
                    .filter(x -> x != null && !x.isEmpty())
                    .collect(Collectors.toSet());

            /* 기존 ID - 유지 ID */
            Set<String> toDeleteIds = new HashSet<>(existIds);
            toDeleteIds.removeAll(maintainIds);
            programVisualizationRepository.deleteAllById(toDeleteIds);

            /* 유지되고 있는 시각화 정보가 있다면 시각화 정보 수정 */
            if (!maintainIds.isEmpty()) {
                for (String visId : maintainIds) {
                    ProgramVisualization target = pgmVisualizations.stream().filter(x -> x.getVisId().equals(visId))
                            .findFirst()
                            .orElse(null);
                    if (target != null) {
                        ProgramVisualizationUpsertDto dto = dtoList.stream().filter(x -> x.getVisId().equals(visId)).findFirst().orElse(null);
                        if (dto != null) target.changeInfo(dto);
                    }
                }
            }

            /* dto 리스트 순회하면서 신규 시각화 만들기 */
            List<ProgramVisualization> newVisList = new ArrayList<>();
            dtoList.stream()
                    .filter(dto -> dto.getVisId() == null || dto.getVisId().isEmpty())
                    .forEach(target -> {
                        newVisList.add(new ProgramVisualization(target));
                    });

            /* bulk insert */
            programVisualizationRepository.saveAll(newVisList);
        }
    }

    @SimulationTx
    public void delete(String visId) {
        programVisualizationRepository.deleteById(visId);
    }

    /* 프로그램 삭제시 해당 프로그램 시각화 삭제 */
    @SimulationTx
    public void deleteAllByPgmId(String pgmId) {
        Set<String> visIds = findAllProgramVisualizations(pgmId)
                .stream().map(ProgramVisualizationDto::getVisId).collect(Collectors.toSet());

        /* 대시보드에서 시각화 항목 사용시 제거한 항목만 유지  */
        dashboardService.updateDsbdVisItemsByVisIds(new ArrayList<>(visIds));

        /* 프로그램ID를 사용하는 시각화 전체 삭제 */
        programVisualizationRepository.deleteAllByPgmId(pgmId);
    }


    public List<ProgramVisualizationDto> findAllProgramVisualizations(String pgmId) {
        return programVisualizationRepository.findAllProgramVisualizations(pgmId);
    }

    public List<ProgramVisualizationDto> findAllProgramVisualizationsByGrpId(String grpId) {
        return programVisualizationRepository.findAllProgramVisualizationsByGrpId(grpId);
    }

    public List<? extends ProgramVisualizationItemDto> getProgramVisualizationItems(String pgmId) {
        try {
            List<ProgramResultDto> list = programResultRepository.findAllProgramResults(pgmId);

            Set<String> enableFileNames = new HashSet<>();

            if (!list.isEmpty()) {
                for (ProgramResultDto result : list) {
                    String fileName = result.getRsltNm();
                    for (String ext : result.getFileXtns().getValidExtensions()) {
                        enableFileNames.add(fileName + ext);
                    }
                }
            }

            List<ProgramVisualizationItemDto> items = new ArrayList<>();
            List<ProgramInputFileDto> allInputs = programInputFileService.findAllProgramInputFiles(pgmId);

            if (!allInputs.isEmpty()) {
                List<MapItemDto> inpFiles = allInputs.stream().filter(x -> x.getFileXtns().equals(FileExtension.INP))
                        .map(x -> {

                            MapItemDto item = new MapItemDto();
                            item.setFileType(MapFiletype.MODEL);
                            item.setFileName(x.getTrgtNm() + ".inp");
                            return item;
                        }).toList();

                if (!inpFiles.isEmpty()) {
                    items.addAll(inpFiles);
                }
            }


            List<File> files = fileManager.getProgramLastResultFiles(pgmId);

            if (files != null && !files.isEmpty()) {
                List<File> enableFiles = files
                        .stream()
                        .filter(x -> enableFileNames.contains(x.getName()))
                        .toList();

                for (ProgramResultDto result : list) {
                    switch (result.getFileXtns()) {
                        case XLSX -> {
                            ExcelAndCsvItemDto excelAndCsvItemDto = new ExcelAndCsvItemDto();
                            List<SheetItem> sheetItems = new ArrayList<>();
                            File targetFile = enableFiles.stream().filter(x -> x.getName().equals(result.getRsltNm() + ".xlsx"))
                                    .findFirst()
                                    .get();

                            Map<String, List<String>> sheetMap = ExcelUtil.getSheetAndHeaders(targetFile.getAbsolutePath());

                            for (Map.Entry<String, List<String>> entry : sheetMap.entrySet()) {
                                SheetItem sheetItem = new SheetItem();
                                sheetItem.setSheetName(entry.getKey());
                                sheetItem.setHeaders(entry.getValue());
                                sheetItems.add(sheetItem);
                            }

                            excelAndCsvItemDto.setItemList(sheetItems);
                            excelAndCsvItemDto.setFileName(targetFile.getName());
                            items.add(excelAndCsvItemDto);
                        }
                        case CSV -> {
                            ExcelAndCsvItemDto excelAndCsvItemDto = new ExcelAndCsvItemDto();
                            List<SheetItem> sheetItems = new ArrayList<>();
                            File targetFile = enableFiles.stream().filter(x -> x.getName().equals(result.getRsltNm() + ".csv"))
                                    .findFirst()
                                    .get();
                            List<String> headers = CsvUtil.getHeaders(targetFile.getAbsolutePath());
                            SheetItem sheetItem = new SheetItem();
                            sheetItem.setHeaders(headers);
                            sheetItems.add(sheetItem);
                            excelAndCsvItemDto.setItemList(sheetItems);
                            excelAndCsvItemDto.setFileName(targetFile.getName());
                            items.add(excelAndCsvItemDto);
                        }
                        case INP -> {
                            MapItemDto mapItemDto = new MapItemDto();
                            File targetFile = enableFiles.stream().filter(x -> x.getName().equals(result.getRsltNm() + ".inp"))
                                    .findFirst()
                                    .get();

                            mapItemDto.setFileName(targetFile.getName());
                            mapItemDto.setFileType(MapFiletype.MODEL);
                            items.add(mapItemDto);
                        }
                        case SHP -> {
                        }
                        case RPT -> {
                            MapItemDto mapItemDto = new MapItemDto();
                            File targetFile = enableFiles.stream().filter(x -> x.getName().equals(result.getRsltNm() + ".rpt"))
                                    .findFirst()
                                    .get();

                            mapItemDto.setFileName(targetFile.getName());
                            mapItemDto.setFileType(MapFiletype.DATA);
                            items.add(mapItemDto);
                        }
                    }
                }
            }
            return items;
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    @SimulationTx
    public void saveResultImage(List<ProgramResult> imageResults) {
        if (imageResults.isEmpty()) return;

        List<String> allIds = imageResults.stream().map(ProgramResult::getRsltId).toList();
        List<ProgramVisualization> exists = programVisualizationRepository.findAllById(allIds);
        Set<String> existsIds = new HashSet<>(exists.stream().map(ProgramVisualization::getVisId).toList());

        List<ProgramVisualization> newVisList = imageResults
                .stream()
                .filter(r -> !existsIds.contains(r.getRsltId()))
                .map(ProgramVisualization::fromImageResult)
                .toList();

        if (!newVisList.isEmpty()) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                for (ProgramVisualization vis : newVisList) {

                    programVisualizationRepository.upsertVisByResult(
                            vis.getVisId(),
                            vis.getPgmId(),
                            vis.getVisNm(),
                            vis.getVisTypeCd().name(),
                            mapper.writeValueAsString(vis.getVisSetupText()),
                            vis.getRgstId(),
                            vis.getRgstDttm(),
                            vis.getMdfId(),
                            vis.getMdfDttm()
                    );
                }
            } catch (Exception e) {
                log.error("saveResultImage error : {}", e.getMessage());
                throw new RestApiException(ProgramErrorCode.PROGRAM_VIS_SETUP_ERROR);
            }
        }
    }

    @SimulationTx
    public void deleteByResultFileName(String fileName) {
        programVisualizationRepository.deleteAllByFileName("$.** ? (@.fileName == $target)", fileName);
    }

    @SimulationTx
    public void bulkUpdateLayerIdsByResultId(String rsltId) {
        programVisualizationRepository.bulkUpdateLayerIdsByResultId(rsltId);
    }

    @Cacheable(
            value = CacheConst.CACHE_1MIN,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(#visId,#histId)"
    )
    public VisualizationItem pgmResultToVisData(String visId, String histId) {
        ProgramVisualizationDto visData = programVisualizationRepository.findByVisId(visId);

        if (visData != null) {
            String pgmId = visData.getPgmId();
            VisualizationItem result = new VisualizationItem();
            result.setVisId(visId);
            List<File> resultFiles;
            ProgramExecHistDto hist;
            if (histId == null || histId.isEmpty()) {
                String rsltDirId = fileManager.getLastUuidDirPath(fileManager.getProgramResultDir(pgmId));
                hist = execHistRepository.findByPgmIdAndRsltDirId(pgmId, rsltDirId);
                result.setHistId(hist.getHistId());
                resultFiles = fileManager.getProgramLastResultFiles(pgmId);
            } else {
                hist = execHistRepository.findByHistId(histId);
                result.setHistId(hist.getHistId());
                resultFiles = fileManager.getProgramResultFiles(pgmId, hist.getRsltDirId());
            }

            VisTypeCd typeCd = visData.getVisTypeCd();
            VisSetupItem visSetupText = visData.getVisSetupText();
            result.setSetup(visSetupText);
            result.setExecStrtDttm(hist.getExecStrtDttm());
            result.setExecEndDttm(hist.getExecEndDttm());
            result.setExecTypeCd(hist.getExecTypeCd());

            if (resultFiles != null && !resultFiles.isEmpty()) {
                /* 시각화 데이터 가공 */
                List<ProgramInputFileDto> inputFiles = programInputFileService.findAllProgramInputFiles(pgmId);
                result.setResult(processor.makeVisData(typeCd, visSetupText, resultFiles, inputFiles));

                return result;
            } else {
                /* 결과파일 부재 */
                throw new RestApiException(ProgramErrorCode.PROGRAM_RESULTSET_NOT_FOUND);
            }
        }
        throw new RestApiException(ProgramErrorCode.PROGRAM_NOT_FOUND);
    }

        public ProgramExecHistDto getPgmVisHistIdByDirection(PgmVisSearchDto dto) {
            String pgmId = getPgmIdByVisId(dto.getVisId());

            if (dto.getHistId() != null && !dto.getHistId().isEmpty()) {
                ProgramExecHistDto hist = execHistRepository.findByHistId(dto.getHistId());
                if (hist == null) throw new RestApiException(ProgramHistErrorCode.NOT_EXISTS_HIST);
                ProgramExecHistDto goal = execHistRepository.findByPgmIdAndRsltDirIdUsingDirection(pgmId, hist.getRsltDirId(), dto.getDirection());
                if (goal != null) {
                    return goal;
                } else {
                    throw new RestApiException(ProgramHistErrorCode.NOT_EXISTS_HIST);
                }
            } else {
                String rsltDirId = fileManager.getLastUuidDirPath(fileManager.getProgramResultDir(pgmId));
                ProgramExecHistDto lastResult = execHistRepository.findByPgmIdAndRsltDirId(pgmId, rsltDirId);
                if (lastResult != null) {
                    return lastResult;
                } else {
                    throw new RestApiException(ProgramHistErrorCode.NOT_EXISTS_HIST);
                }
            }
        }


    public List<ProgramExecHistDto> getPgmVisHitIdByTime(String visId, LocalDateTime time) {
        ProgramDto findProgram = programRepository.findProgramByVisId(visId);

        if (findProgram.getRltmYn() == YesOrNo.Y) {
            ChronoUnit unit = findProgram.getRpttIntvTypeCd().getUnit();
            Integer interval = findProgram.getRpttIntvVal();
            LocalDateTime startDttm = time.minus(interval * 2, unit);
            LocalDateTime endDttm = time.plus(interval * 2, unit);
            return execHistRepository.findByPgmIdAndExecStrtDttm(findProgram.getPgmId(), startDttm, endDttm);
        } else {
            String pgmId = findProgram.getPgmId();
            String lastDirId = fileManager.getLastUuidDirPath(fileManager.getProgramResultDir(findProgram.getPgmId()));
            DirectionType direction = DirectionType.PREV;
            Integer limit = 5;
            return execHistRepository.findByPgmIdAndRsltDirIdUsingDirection(pgmId, lastDirId, direction, limit);
        }
    }


    private String getPgmIdByVisId(String visId) {
        return programVisualizationRepository.findByVisId(visId).getPgmId();
    }
}
