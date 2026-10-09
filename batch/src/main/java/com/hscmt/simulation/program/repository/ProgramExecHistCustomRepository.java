package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.ProgramExecHist;

import java.util.List;

public interface ProgramExecHistCustomRepository {

    List<ProgramExecHist> findByPgmIdLimit(String pgmId, Long limitCount);
}
