package com.hscmt.simulation.layer.mapper;

import com.hscmt.simulation.common.config.mybatis.SimulationMapper;
import com.hscmt.simulation.layer.dto.LayerListUpsertDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@SimulationMapper
public interface LayerMapper {

    /* 레이어 내역 저장 */
    void upsertLayerList(LayerListUpsertDto dto);

    void updateLayerUseAble(@Param("layerId") String layerId,  @Param("executorId") String executorId, @Param("featureType") String featureType);

    void updateLayerLayerProperties (@Param("layerId") String layerId, @Param("properties") String properties);
}
