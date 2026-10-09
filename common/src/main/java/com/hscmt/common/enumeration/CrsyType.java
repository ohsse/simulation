package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum CrsyType {
    EPSG5181("EPSG:5181","epsg=5181", 5181),
    EPSG5186("EPSG:5186","epsg=5186", 5186),
    EPSG3857("EPSG:3857","epsg=3857", 3857),
    EPSG4326("EPSG:4326","epsg=4326", 4326),
    EPSG5179("EPSG:5179","epsg=5179", 5179),
    ;

    private final String epsgName;
    private final String epsgPyCode;
    private final Integer srid;
}
