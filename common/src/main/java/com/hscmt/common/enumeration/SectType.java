package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SectType {
    TITLE("[TITLE]"),
    JUNCTIONS("[JUNCTIONS]", new String[]{ "ID", "ELEVATION", "BASE_DEMAND", "PATTERN_ID" }, new String[]{";ID", "Elev", "Demand", "Pattern", "Remark"}),
    RESERVOIRS("[RESERVOIRS]", new String[]{ "ID", "HEAD", "PATTERN_ID" }, new String[]{";ID", "Head", "Pattern", "Remark"}),
    TANKS("[TANKS]", new String[]{ "ID", "ELEVATION", "INITLVL", "MINLVL", "MAXLVL", "DIAMETER", "MINVOL","VOLCURVE" }, new String[]{";ID","Elevation", "InitLevel", "MinLevel", "MaxLevel", "Diameter", "MinVol", "VolCurve", "Overflow"}),
    PIPES("[PIPES]", new String[]{ "ID", "NODE1", "NODE2", "LENGTH", "DIAMETER", "ROUGHNESS", "MLOSS","STATUS" }, new String[]{";ID","Node1", "Node2", "Length", "Diameter", "Roughness", "MinorLoss","Status","Remark"}),
    PUMPS("[PUMPS]", new String[]{ "ID", "NODE1", "NODE2", "PARAMETERS" }, new String[]{";ID","Node1", "Node2", "Parameters", "Remark"}),
    VALVES("[VALVES]", new String[]{ "ID", "NODE1", "NODE2", "DIAMETER", "TYPE", "SETTING", "MINORLOSS" }, new String[]{";ID","Node1", "Node2", "Diameter", "Type", "Setting", "MinorLoss", "Remark"}),
    CONTROLS("[CONTROLS]", new String[]{ "STATEMENT" }),
    RULES("[RULES]", new String[] { "STATEMENT" }),
    DEMANDS("[DEMANDS]", new String[]{ "ID", "DEMAND", "PATTERN_ID" }, new String[]{";Junction", "Demand", "Pattern","Category"}),
    SOURCES("[SOURCES]", new String[]{ "ID", "TYPE", "QUALITY", "PATTERN_ID" }, new String[]{";Node", "Type","Quality", "Pattern"}),
    EMITTERS("[EMITTERS]", new String[]{ "ID", "COEFFICIENT" }, new String[]{";Junction", "Coefficient"}),
    QUALITY("[QUALITY]", new String[]{ "ID", "INITQUAL" }, new String[]{";Node", "InitQual" }),
    STATUS("[STATUS]", new String[]{ "ID", "STATUS_SETTING" }, new String[]{";ID", "Status/Setting"}),
    CURVES("[CURVES]", new String[]{ "ID", "X", "Y" }, new String[]{";ID","X-Value","Y-Value"}),
    PATTERNS("[PATTERNS]", new String[]{ "ID", "MULTIPLIERS" }, new String[]{";ID", "Multipliers"}),
    ENERGY("[ENERGY]", new String[]{"GLOBAL_EFFICIENCY", "GLOBAL_PRICE", "DEMAND_CHARGE", "PUMP" }),
    REACTIONS("[REACTIONS]", new String[]{ "ORDER_BULK", "ORDER_TANK","ORDER_WALL","GLOBAL_BULK", "GLOBAL_WALL", "LIMITING_POTENTIAL", "ROUGHNESS_CORRELATION" }),
    REACTIONS_PT("[REACTIONS]", new String[]{ "TYPE", "ID", "COEFFICIENT" }, new String[]{";Type", "Pipe/Tank", "Coefficient"}),
    MIXING("[MIXING]", new String[]{ "ID", "MODEL", "FRACTION" }, new String[]{";Tank", "Model"}),
    REPORT("[REPORT]", new String[]{ "STATUS", "SUMMARY", "PAGE",  "NODES", "ENERGY", "LINKS", "Elevation", "Demand", "Head", "Pressure", "Quality" , "Length", "Diameter", "Flow", "Velocity" , "Headloss", "Status" , "Setting", "Reaction"}),
    TIMES("[TIMES]", new String[]{ "DURATION", "HYDRAULIC_TIMESTEP", "QUALITY_TIMESTEP","PATTERN_TIMESTEP", "PATTERN_START", "REPORT_TIMESTEP", "REPORT_START", "START_CLOCKTIME", "STATISTIC" }),
    OPTIONS("[OPTIONS]", new String[]{ "UNITS", "PRESSURE", "HEADLOSS", "SPECIFIC_GRAVITY", "VISCOSITY", "TRIALS",
            "ACCURACY", "CHECKFREQ", "MAXCHECK", "DAMPLIMIT", "UNBALANCED", "PATTERN", "DEMAND_MULTIPLIER",
            "EMITTER_EXPONENT", "QUALITY", "DIFFUSIVITY", "TOLERANCE" }),
    COORDINATES("[COORDINATES]", new String[]{ "ID", "X", "Y" }, new String[]{";Node" ,"X-Coord", "Y-Coord"}),
    VERTICES("[VERTICES]", new String[]{ "ID", "X", "Y" }, new String[]{";Link" ,"X-Coord", "Y-Coord"}),
    LABELS("[LABELS]", new String[]{ "X", "Y", "LABEL" }, new String[]{";X-Coord", "Y-Coord", "Label & Anchor Node" }),
    TAGS("[TAGS]", new String[]{ "TYPE", "ID", "INFO" }, new String[]{";Type", "ID", "Info" }),
    BACKDROP("[BACKDROP]", new String[]{"DIMENSIONS", "UNITS", "OFFSET"}),
    END("[END]");
    private final String inpTitle;
    private final String[] sectionFields;
    private final String[] inpFields;

    SectType (String inpTitle) {this (inpTitle, new String[]{});}
    SectType (String inpTitle, String[] sectionFields) {this (inpTitle, sectionFields, new String[]{});}
}


