package com.hscmt.simulation.dataset.repository;

import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureSearchDto;

import java.util.List;
import java.util.Map;

public interface MeasureListCustomRepository {
    List<Map<String, Object>> findMeasureList(List<MeasureDatasetDetailDto> targetTags, MeasureSearchDto dto);
}
