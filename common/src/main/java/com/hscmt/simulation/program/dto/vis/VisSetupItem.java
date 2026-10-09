package com.hscmt.simulation.program.dto.vis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChartSetupItemDto.class, name = "CHART"),
        @JsonSubTypes.Type(value = GridSetupItemDto.class, name = "GRID"),
        @JsonSubTypes.Type(value = MapSetupItemDto.class, name = "MAP"),
        @JsonSubTypes.Type(value = ImageSetupItemDto.class, name = "IMAGE")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public interface VisSetupItem {
    String getType();
    String getId();
    String getLabel();
}
