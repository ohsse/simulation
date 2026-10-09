package com.hscmt.simulation.dataset.repository;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDto;

import java.util.List;

public interface MeasureDatasetCustomRepository {

    List<MeasureDatasetDto> findAllDatasets(YesOrNo rltmYn);

    List<MeasureDatasetDto> findDatasetDetailInfoByDsId(String dsId);

    List<MeasureDatasetDto> findAllDatasetDetailInfo();
}
