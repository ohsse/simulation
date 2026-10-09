package com.hscmt.simulation.program.repository;

import com.hscmt.common.enumeration.DirectionType;
import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import com.hscmt.simulation.program.dto.ProgramExecSearchDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ProgramExecHistCustomRepository {

    void deleteAllByPgmId(String pgmId);
    List<ProgramExecHistDto> findAllProgramExecHistList (ProgramExecSearchDto searchDto);
    ProgramExecHistDto findByHistId (String histId);
    ProgramExecHistDto findByPgmIdAndRsltDirId (String pgmId, String rsltDirId);
    ProgramExecHistDto findByPgmIdAndRsltDirIdUsingDirection (String pgmId, String rsltDirId, DirectionType direction);
    List<ProgramExecHistDto> findByPgmIdAndRsltDirIdUsingDirection (String pgmId, String rsltDirId, DirectionType direction, Integer limit);
    List<ProgramExecHistDto> findByPgmIdAndExecStrtDttm(String pgmId, LocalDateTime startDateDttm, LocalDateTime endDateDttm);
}
