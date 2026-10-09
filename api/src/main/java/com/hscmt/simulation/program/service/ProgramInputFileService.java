package com.hscmt.simulation.program.service;

import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dataset.comp.DatasetFileManager;
import com.hscmt.simulation.program.domain.ProgramInputFile;
import com.hscmt.simulation.program.domain.ProgramResult;
import com.hscmt.simulation.program.dto.ProgramInputFileDto;
import com.hscmt.simulation.program.dto.ProgramInputFileUpsertDto;
import com.hscmt.simulation.program.event.ProgramInputFileEventPublisher;
import com.hscmt.simulation.program.repository.ProgramInputFileRepository;
import com.hscmt.simulation.program.repository.ProgramResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
public class ProgramInputFileService {
    private final ProgramInputFileRepository programInputFileRepository;
    private final ProgramInputFileEventPublisher publisher;
    private final ProgramResultRepository programResultRepository;
    private final DatasetFileManager fileManager;

    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void save (String pgmId, List<ProgramInputFileUpsertDto> dtoList) {

        if ( dtoList != null ) {
            /* 프로그램이 가지고 있는 모든 input 파일 목록 조회 */
            List<ProgramInputFile> pgmInputFiles = programInputFileRepository.findAllByPgmId(pgmId);

            /* 기존 ID */
            Set<String> existIdSet = new HashSet<>(pgmInputFiles.stream().map(ProgramInputFile::getInputFileId).toList());
            /* 유지시킬 ID */
            Set<String> maintainIds = dtoList.stream()
                    .map(ProgramInputFileUpsertDto::getInputFileId)
                    .filter(x -> x != null && !x.isEmpty())
                    .collect(Collectors.toSet());

            /* 기존 저장되었던 id에서 유지시킬 id를 제외 한다. */
            Set<String> deleteIds = new HashSet<>(existIdSet);
            deleteIds.removeAll(maintainIds);
            /* 기존 ID - 유지 ID = 삭제 ID */
            deleteAllByInputFileIds(deleteIds);

            /* dto 리스트를 순회하면서 id가 없다면 신규 input 파일 */
            List<ProgramInputFile> newFiles = new ArrayList<>();
            dtoList.stream()
                    .filter(dto -> dto.getInputFileId() == null || dto.getInputFileId().isEmpty())
                    .forEach(upsertTarget -> {
                        upsertTarget.setPgmId(pgmId);
                        newFiles.add(new ProgramInputFile(upsertTarget));
                    });

            /* bulk insert 로 저장 */
            programInputFileRepository.saveAll(newFiles);
        }
    }

    @SimulationTx
    public void delete (String inputFileId) {
        programInputFileRepository.deleteById(inputFileId);
    }

    @SimulationTx
    public void deleteAllByDsId (String dsId, List<String> fileNames) {
        /* 삭제하려는 데이터셋 id 로 쓰는 프로그램 input파일 전부 검색 */
        List<ProgramInputFile> deleteEntities = programInputFileRepository.findAllByDsId(dsId);
        /* 삭제하고 이벤트 발행 */
        publisher.deleteAllAndPublish(deleteEntities, fileNames);
    }

    @SimulationTx
    public void deleteAllByRsltId (String rsltId, List<String> fileNames) {
        /* 삭제하려는 프로그램 결과 id로 쓰는 프로그램 input파일 전부 검색 */
        List<ProgramInputFile> deleteEntities = programInputFileRepository.findAllByRsltId(rsltId);
        /* 삭제하고 이벤트 발행 */
        publisher.deleteAllAndPublish(deleteEntities, fileNames);
    }

    public List<ProgramInputFileDto> findAllProgramInputFiles (String pgmId) {
        List<ProgramInputFileDto> datasetInputFiles = programInputFileRepository.findAllDatasetInputFiles( pgmId );
        List<ProgramInputFileDto> resultInputFiles = programInputFileRepository.findAllResultInputFiles( pgmId );

        List<ProgramInputFileDto> inputFiles = new ArrayList<>();
        inputFiles.addAll(datasetInputFiles);
        inputFiles.addAll(resultInputFiles);

        return inputFiles;
    }

    public List<ProgramInputFileDto> findAllProgramInputFilesByGrpId (String grpId) {
        List<ProgramInputFileDto> datasetInputFiles = programInputFileRepository.findAllDatasetInputFilesByGrpId( grpId );
        List<ProgramInputFileDto> resultInputFiles = programInputFileRepository.findAllResultInputFilesByGrpId( grpId );

        List<ProgramInputFileDto> inputFiles = new ArrayList<>();
        inputFiles.addAll(datasetInputFiles);
        inputFiles.addAll(resultInputFiles);

        return inputFiles;
    }



    @SimulationTx
    public void deleteAllByInputFileIds(Set<String> inputFileIds) {
        List<ProgramInputFile> deleteEntities = programInputFileRepository.findAllById(inputFileIds);

        for (ProgramInputFile entity : deleteEntities) {
            switch (entity.getTrgtType()) {
                case DATASET -> publisher.publishAndClear(entity, fileManager.getDatasetFileNames(entity.getTrgtId()));
                case PROGRAM_RESULT -> {
                    ProgramResult programResult = programResultRepository.findById(entity.getTrgtId()).orElse(null);
                    if (programResult != null) {
                        publisher.publishAndClear(entity, programResult.getFileXtns().getValidExtensions()
                                .stream()
                                .map(ext -> programResult.getRsltNm() + ext).toList());
                    }
                }
            }

        }
        programInputFileRepository.deleteAllInBatch(deleteEntities);
    }
}
