package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, String>, ProgramCustomRepository {
}
