package com.hscmt.simulation.venv.repository;

import com.hscmt.simulation.venv.domain.VirtualEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenvRepository extends JpaRepository<VirtualEnvironment, String> , VenvCustomRepository{
}
