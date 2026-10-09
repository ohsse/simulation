package com.hscmt.simulation.dataset.dto.pn;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PipeNetworkJsonResponse.class, names = "vector"),
        @JsonSubTypes.Type(value = PipeNetworkUrlResponse.class, name = "raster")
})
public interface PipeNetworkVisResponse {
    String getType();
}
