package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.ProgramVisualization;
import com.hscmt.simulation.program.dto.ProgramVisualizationDto;

import java.util.List;

public interface ProgramVisualizationCustomRepository {

    void deleteAllByPgmId(String pgmId);
    List<ProgramVisualization> findAllByPgmId(String pgmId);
    List<ProgramVisualizationDto> findAllProgramVisualizations ( String pgmId );
    List<ProgramVisualizationDto> findAllProgramVisualizationsByGrpId ( String grpId );
    ProgramVisualizationDto findByVisId ( String visId );
}
