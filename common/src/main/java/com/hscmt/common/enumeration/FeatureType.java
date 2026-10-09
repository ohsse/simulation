package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
@RequiredArgsConstructor
public enum FeatureType {
    POINT(new String[]{"POINT", "MULTIPOINT"}),
    LINE(new String[]{"LINESTRING", "MULTILINESTRING"}),
    POLYGON(new String[]{"POLYGON", "MULTIPOLYGON"});

    private final String[] types;
    private static final Map<String, FeatureType> LOOKUP;
    static {
        Map<String, FeatureType> m = new HashMap<>();
        for (FeatureType ft : values()) {
            for (String t : ft.types) {
                m.put(t.toUpperCase(Locale.ROOT), ft);
            }
        }
        LOOKUP = Collections.unmodifiableMap(m);
    }

    /** "Point", "MULTIPOLYGON" 등 타입명으로 찾기 */
    public static Optional<FeatureType> fromTypeName(String typeName) {
        if (typeName == null) return Optional.empty();
        return Optional.ofNullable(LOOKUP.get(typeName.toUpperCase(Locale.ROOT)));
    }
}