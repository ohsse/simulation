package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.ProgramResult;
import com.hscmt.simulation.program.dto.ProgramResultDto;

import java.util.List;

public interface ProgramResultCustomRepository {
    List<ProgramResult> findAllByPgmId(String pgmId);
    List<ProgramResultDto> findAllProgramResults (String pgmId);
    List<ProgramResultDto> findAllProgramResultsByGrpId (String grpId);
    void deleteAllByPgmId(String pgmId);
}
