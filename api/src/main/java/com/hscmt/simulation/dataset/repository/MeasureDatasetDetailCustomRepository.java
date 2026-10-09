package com.hscmt.simulation.dataset.repository;

import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;

import java.util.List;

public interface MeasureDatasetDetailCustomRepository {

    void deleteAllByDsId(String dsId);
    List<MeasureDatasetDetailDto> findAllByDsId(String dsId);
}
