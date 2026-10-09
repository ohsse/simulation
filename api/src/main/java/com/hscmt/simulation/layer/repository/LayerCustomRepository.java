package com.hscmt.simulation.layer.repository;

import com.hscmt.simulation.layer.dto.LayerDto;

import java.util.List;

public interface LayerCustomRepository {
    List<LayerDto> findAllLayers(String grpId);
    LayerDto findLayerDtoById (String layerId);
    void updateGrpIdToNull (String grpId);
    void updateGrpIdToNull (List<String> grpIds);
}
