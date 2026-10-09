package com.hscmt.simulation.tag.mapper;

import com.hscmt.simulation.common.config.mybatis.SimulationMapper;

@SimulationMapper
public interface WaternetTagMapper {
    void updateNoneCollectableTag ();
    void updateCollectableTag ();
}
