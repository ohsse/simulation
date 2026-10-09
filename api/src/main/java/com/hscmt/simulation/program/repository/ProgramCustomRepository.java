package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.dto.ProgramDto;

import java.util.List;

public interface ProgramCustomRepository {

    void updateProgramVenvToNull (String venvId);
    List<ProgramDto> findAllPrograms(String grpId);
    ProgramDto findProgramById(String pgmId);
    void updateGrpIdToNull (String grpId);
    void updateGrpIdToNull (List<String> grpIds);
    ProgramDto findProgramByVisId (String visId);
}
