package com.hscmt.simulation.collect.mapper;

import com.hscmt.simulation.collect.dto.MsrmUpsertDto;
import com.hscmt.simulation.common.config.mybatis.SimulationMapper;
import com.hscmt.simulation.dataset.dto.WaternetTagDto;
import org.apache.ibatis.annotations.Param;

@SimulationMapper
public interface CollectMapper {
    void upsertTagData (MsrmUpsertDto upsertDto);
}
