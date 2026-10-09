package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.ProgramInputFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramInputFileRepository extends JpaRepository<ProgramInputFile, String>, ProgramInputFileCustomRepository {
}
