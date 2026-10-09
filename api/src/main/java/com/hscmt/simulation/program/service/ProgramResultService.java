package com.hscmt.simulation.program.service;

import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.layer.service.LayerService;
import com.hscmt.simulation.program.domain.ProgramResult;
import com.hscmt.simulation.program.dto.ProgramResultDto;
import com.hscmt.simulation.program.dto.ProgramResultUpsertDto;
import com.hscmt.simulation.program.event.ProgramResultEventPublisher;
import com.hscmt.simulation.program.repository.ProgramResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
public class ProgramResultService {
    private final ProgramResultRepository programResultRepository;
    private final ProgramResultEventPublisher publisher;
    private final LayerService layerService;
    private final ProgramVisualizationService visualizationService;

    @SimulationTx
    public void save(String pgmId, List<ProgramResultUpsertDto> dtoList) {
        if ( dtoList != null ) {
            /* 기존 프로그램결과 목록 */
            List<ProgramResult> pgmResults = programResultRepository.findAllByPgmId(pgmId);
            /* 기존 ID SET */
            Set<String> existIdSet = new HashSet<>(pgmResults.stream().map(ProgramResult::getRsltId).toList());
            /* 유지 ID SET */
            Set<String> maintainIds = dtoList.stream()
                    .map(ProgramResultUpsertDto::getRsltId)
                    .filter(x -> x != null && !x.isEmpty())
                    .collect(Collectors.toSet());


            /* 삭제 : 기존 - 유지 */
            Set<String> deleteIds = new HashSet<>(existIdSet);
            deleteIds.removeAll(maintainIds);

            if (!maintainIds.isEmpty()) {
                for (String rsltId : maintainIds) {
                    ProgramResult target = pgmResults.stream().filter(x -> x.getRsltId().equals(rsltId))
                            .findFirst()
                            .orElse(null);

                    if (target != null) {
                        ProgramResultUpsertDto dto = dtoList.stream()
                                .filter( x-> x.getRsltId().equals(rsltId))
                                .findFirst()
                                .orElse(null);

                        if (dto != null) target.changeInfo (dto);
                    }
                }
            }

            /* 삭제하고 이벤트 발행 */
            publisher.deleteAllAndPublish(programResultRepository.findAllById(deleteIds));

            List<ProgramResult> newResults = new ArrayList<>();
            dtoList.stream()
                    .filter(dto -> dto.getRsltId() == null || dto.getRsltId().isEmpty())
                    .forEach(target -> {
                        target.setPgmId(pgmId);
                        newResults.add(new ProgramResult(target));
                    });

            /* bulk insert */
            List<ProgramResult> savedResults = programResultRepository.saveAll(newResults);

            /* image 결과 파일은 시각화에 자동등록 */
            visualizationService.saveResultImage(savedResults.stream().filter(x -> x.getFileXtns() == FileExtension.PNG || x.getFileXtns() == FileExtension.JPG).toList());

            /* shp 결과 파일은 레이어에 자동등록 */
            layerService.saveResultLayer(savedResults.stream().filter(x -> x.getFileXtns() == FileExtension.SHP).toList());
        }
    }

    public List<ProgramResultDto> findAllProgramResults ( String pgmId ) {
        return programResultRepository.findAllProgramResults(pgmId);
    }

    public List<ProgramResultDto> findAllProgramResultsByGrpId ( String grpId ) {
        return programResultRepository.findAllProgramResults(grpId);
    }


    @SimulationTx
    public void deleteAllByPgmId(String pgmId) {
        publisher.deleteAllAndPublish(programResultRepository.findAllByPgmId(pgmId));
    }
}
