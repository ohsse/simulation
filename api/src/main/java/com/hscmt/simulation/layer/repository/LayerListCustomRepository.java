package com.hscmt.simulation.layer.repository;

import com.hscmt.simulation.layer.dto.LayerFeatureDto;

import java.util.List;

public interface LayerListCustomRepository {
    void deleteAllByLayerId (String layerId);
    List<LayerFeatureDto> findLayerFeatures (String layerId, Double minX, Double minY, Double maxX, Double maxY);
    LayerFeatureDto findLayerFeatureInfo (String layerId, Long fid);
}
