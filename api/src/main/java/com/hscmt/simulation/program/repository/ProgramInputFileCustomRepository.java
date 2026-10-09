package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.hscmt.simulation.dataset.dto.DatasetSearchDto;
import com.hscmt.simulation.program.domain.ProgramInputFile;
import com.hscmt.simulation.program.dto.ProgramInputFileDto;

import java.util.List;

public interface ProgramInputFileCustomRepository {

    void deleteAllByPgmId(String pgmId);
    List<ProgramInputFile> findAllByPgmId(String pgmId);
    List<ProgramInputFile> findAllByDsId(String dsId);
    List<ProgramInputFile> findAllByRsltId (String rsltId);
    List<DatasetDto> groupingForDataset (DatasetSearchDto dto);
    List<ProgramInputFileDto> findAllDatasetInputFiles (String pgmId);
    List<ProgramInputFileDto> findAllResultInputFiles (String pgmId);
    List<ProgramInputFileDto> findAllDatasetInputFilesByGrpId (String grpId);
    List<ProgramInputFileDto> findAllResultInputFilesByGrpId (String grpId);
}
