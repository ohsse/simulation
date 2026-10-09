package com.hscmt.simulation.program.service;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import com.hscmt.simulation.layer.service.LayerService;
import com.hscmt.simulation.program.comp.ProgramFileManager;
import com.hscmt.simulation.program.comp.ProgramValidator;
import com.hscmt.simulation.program.domain.Program;
import com.hscmt.simulation.program.dto.*;
import com.hscmt.simulation.program.error.ProgramErrorCode;
import com.hscmt.simulation.program.event.ProgramEventPublisher;
import com.hscmt.simulation.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@SimulationTx(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramFileManager fileManager;
    private final ProgramValidator validator;
    private final ProgramEventPublisher publisher;
    private final ProgramInputFileService programInputFileService;
    private final ProgramVisualizationService programVisualizationService;
    private final ProgramResultService programResultService;

    @SimulationTx
    public void save(ProgramUpsertDto dto, MultipartFile file) {
        String pgmId = dto.getPgmId();
        Program saveProgram;
        /* 프로그램 ID 가 없다면 신규 프로그램으로 등록 */
        if (pgmId == null || pgmId.isEmpty()) {
            /* 신규 프로그램 검증 */
            validator.validateNewProgram(dto, file);
            /* 프로그램 저장 후 이벤트 발행 */
            saveProgram = publisher.saveAndPublish(new Program(dto));
        } else {
            /* 프로그램이 존재한다면 */
            saveProgram = programRepository.findById(pgmId)
                    .orElseThrow(() -> new RestApiException(ProgramErrorCode.PROGRAM_NOT_FOUND));
            /* 정보 수정 후 이벤트 발행 */
            publisher.updateAndPublish(saveProgram, dto);
        }
        /* 파일 업로드 */
        String saveDirPath = fileManager.uploadFile(saveProgram.getPgmId(), file);

        if (saveDirPath != null) {
            saveProgram.changeFnlPdirId( saveDirPath );
        }

        /* 프로그램 시각화 등록 */
        programVisualizationService.save(saveProgram.getPgmId(), dto.getVisualizations());
        /* 프로그램 결과 파일 설정 */
        programResultService.save(saveProgram.getPgmId(), dto.getResults());
        /* 프로그램 input 파일 설정 */
        programInputFileService.save(saveProgram.getPgmId(), dto.getInputFiles());
    }

    /* 프로그램 삭제 */
    @SimulationTx
    public void delete (String pgmId) {
        /* 프로그램 시각화 삭제 */
        programVisualizationService.deleteAllByPgmId(pgmId);
        /* 프로그램 결과 삭제 -> 프로그램 결과를 input으로 쓰는 프로그램 input설정도 삭제 && 프로그램 결과로 쓰는 레이어도 삭제 */
        programResultService.deleteAllByPgmId(pgmId);
        /* 프로그램 삭제 */
        programRepository.findById(pgmId)
                .ifPresent(findProgram -> {
                    try {
                        publisher.deleteAndPublish(findProgram);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public List<ProgramDto> findAllPrograms (String grpId) {
        
        List<ProgramDto> programs = programRepository.findAllPrograms(grpId);
        
        List<ProgramResultDto> programResults = programResultService.findAllProgramResultsByGrpId( grpId );
        List<ProgramVisualizationDto> programVisualizations = programVisualizationService.findAllProgramVisualizationsByGrpId( grpId );
        List<ProgramInputFileDto> inputFiles = programInputFileService.findAllProgramInputFilesByGrpId( grpId );

        programs.forEach(program -> {
            program.setResults(programResults.stream()
                    .filter(programResultDto -> programResultDto.getPgmId().equals(program.getPgmId()))
                    .toList());
            program.setInputFiles(inputFiles.stream()
                    .filter(inputFile -> inputFile.getPgmId().equals(program.getPgmId()))
                    .toList());
            program.setVisualizations(programVisualizations.stream()
                    .filter(programVisualizationDto -> programVisualizationDto.getPgmId().equals(program.getPgmId()))
                    .toList());
            program.setPgmFileInfo(fileManager.getLastProgramInfo(program.getPgmId()));
        });
        return programs;
    }

    /* 프로그램 상세 조회 */
    public ProgramDto findProgram (String pgmId) {
        ProgramDto programDto = programRepository.findProgramById(pgmId);
        if (programDto != null) {
            programDto.setResults(programResultService.findAllProgramResults( pgmId ));
            programDto.setInputFiles(programInputFileService.findAllProgramInputFiles( pgmId ));
            programDto.setVisualizations(programVisualizationService.findAllProgramVisualizations( pgmId ));
            programDto.setPgmFileInfo(fileManager.getLastProgramInfo(pgmId));
        }
        return programDto;
    }

    @SimulationTx
    public void updateGrpIdToNull (String grpId) {
        programRepository.updateGrpIdToNull(grpId);
    }

    @SimulationTx
    public void updateGrpIdToNull (List<String> grpId) {
        programRepository.updateGrpIdToNull(grpId);
    }

    @SimulationTx
    public void updateGroupItems(List<GroupItemUpsertDto> items) {
        for (GroupItemUpsertDto item : items) {
            if (item instanceof ProgramUpsertDto p) {
                programRepository.findById(p.getPgmId())
                        .ifPresent(findProgram -> findProgram.changeGrpInfo(item.getGrpId(), item.getSortOrd()));
            }
        }
    }
}
