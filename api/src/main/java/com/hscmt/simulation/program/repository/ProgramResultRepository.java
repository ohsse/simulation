package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.ProgramResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramResultRepository extends JpaRepository<ProgramResult, String>, ProgramResultCustomRepository {

}
