package com.hscmt.simulation.dataset.repository;


import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.simulation.dataset.domain.Dataset;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.hscmt.simulation.dataset.dto.DatasetSearchDto;

import java.util.List;

public interface DatasetCustomRepository {
    DatasetDto findDatasetDtoById (String dsId, DatasetType dsTypeCd);
    Dataset findDatasetById (String dsId, DatasetType dsTypeCd);
    List<DatasetDto> findAllDatasets (DatasetSearchDto dto);
    void updateGrpIdToNull (String grpId);
    void updateGrpIdToNull (List<String> grpIds);
}

