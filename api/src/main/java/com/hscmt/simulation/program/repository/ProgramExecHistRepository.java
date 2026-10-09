package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.ProgramExecHist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramExecHistRepository extends JpaRepository<ProgramExecHist, String>, ProgramExecHistCustomRepository {
}
