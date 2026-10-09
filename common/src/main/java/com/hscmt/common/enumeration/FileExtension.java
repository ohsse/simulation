package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum FileExtension {
    XLSX(List.of(".xlsx")),
    CSV(List.of(".csv")),
    TXT(List.of(".txt")),
    JPG(List.of(".jpg")),
    PNG(List.of(".png")),
    INP(List.of(".inp")),
    SHP(List.of(".shp",".dbf",".shx",".prj",".cpg",".sbx",".sbn"), List.of(".shp",".dbf",".shx")),
    TIFF(List.of(".tiff", "tif",".geotiff")),
    TTF(List.of(".ttf")),
    RPT(List.of(".rpt")),
    PKL(List.of(".pkl")),
    H2O(List.of("")),
    ;
    private final List<String> validExtensions;
    private final List<String> requiredExtensions;

    FileExtension(List<String> validExtensions) {
        this(validExtensions, List.of());
    }
}
