package com.hscmt.simulation.layer.event;

import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.simulation.layer.dto.LayerStyleInfo;

import java.util.List;

public record LayerUpsertedEvent(String layerId, CrsyType crsyType, List<LayerStyleInfo> styleInfo, String executorId) {
}
