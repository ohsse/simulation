package com.hscmt.simulation.venv.repository;

import com.hscmt.simulation.venv.dto.VenvDto;

import java.util.List;

public interface VenvCustomRepository {
    List<VenvDto> findAllVenvs(String pyVrsn);
    VenvDto findVenvById(String venvId);
    void createComplete(String venvId);
}
