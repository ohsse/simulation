package com.hscmt.simulation.dataset.repository;

import com.hscmt.simulation.dataset.domain.WaternetTag;
import com.hscmt.simulation.dataset.dto.WaternetTagDto;

import java.util.List;
import java.util.Set;

public interface WaternetTagCustomRepository {
    List<WaternetTag> findAllTagsByIds(Set<String> tagSns);

    List<WaternetTagDto> findAllWaternetTags(WaternetTagDto dto);
}
