package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConditionType {
    EQ("equal"),
    NEQ("not equal"),
    GT("greater than"),
    GOE("greater than or equal"),
    LT("less than"),
    LOE("less than or equal"),
    CONTAINS("contains"),
    STARTS_WITH("starts with"),
    ENDS_WITH("ends with"),
    RANGE("(gt goe) x (lt loe)")
    ;

    private final String description;
}
