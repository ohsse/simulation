package com.hscmt.simulation.layer.dto;

import com.hscmt.common.enumeration.CrsyType;

import java.util.List;

public record LayerUpsertRequest(String layerId, CrsyType crsyType, List<LayerStyleInfo> styleInfo, String executorId) {
}
