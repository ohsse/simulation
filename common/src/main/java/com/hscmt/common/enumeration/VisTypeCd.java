package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisTypeCd {
    MAP("관망해석"),
    CHART("차트"),
    GRID("표"),
    IMAGE("이미지"),
    ;
    private final String description;
}
