package com.hscmt.simulation.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemLayoutDto {
    private Integer h;
    private String i;
    private Integer w;
    private Integer x;
    private Integer y;
    private Boolean isBounded;
    private Boolean isDraggable;
    private Boolean isResizable;
    private Double maxH;
    private Double maxW;
    private Double minH;
    private Double minW;
    private Boolean moved;
    private Object[] resizeHandles;
}
