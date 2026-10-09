package com.hscmt.common.util;

import com.hscmt.common.enumeration.SectType;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

@Slf4j
public class InpComposer {
    /* INP MAP To INP 파일 */
    public static void composeMapToInpFile (Map<SectType, List<Map<String, Object>>> inpMap, File saveFile, String charset) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile), charset))) {
            for (SectType sectType : inpMap.keySet()) {
                List<Map<String, Object>> sectList = inpMap.getOrDefault(sectType, new ArrayList<>());
                bw.write(sectType.getInpTitle());
                bw.newLine();
                String[] fields = sectType.getInpFields();

                if (sectType != SectType.REACTIONS
                        && sectType != SectType.BACKDROP
                        && sectType != SectType.CONTROLS
                        && sectType != SectType.RULES
                        && sectType != SectType.TIMES
                        && sectType != SectType.REPORT
                        && sectType != SectType.OPTIONS
                        && sectType != SectType.ENERGY
                )
                for (int i = 0; i < fields.length; i++) {
                    bw.write(StringUtil.convertStartLetterToUpper(fields[i]));
                    bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                }
                if (fields.length > 0) bw.newLine();
                writeSectionContent(bw, sectType, sectList);
                bw.newLine();
            }
        } catch (Exception e) {
            log.error("map convert to InpFile Error : {}", e.getMessage());
        }
    }

    /* sectType  별 writing */
    public static void writeSectionContent (BufferedWriter bw, SectType sectType, List<Map<String, Object>> sectList) throws IOException {
        String [] fields = sectType.getSectionFields();
        switch (sectType) {
            case TITLE -> writeSimpleSection(bw, sectList, sectType.name());
            case JUNCTIONS, RESERVOIRS, TANKS, PIPES, PUMPS, VALVES -> writeDelimitedSection(bw, sectList, sectType);
            case CONTROLS -> {
                for (Map<String, Object> control : sectList) {
                    for (int i = 0; i < fields.length; i++) {
                        writeIfNotNull(bw, control, fields[i]);
                    }
                    if (control.get("REMARK") != null) {
                        bw.write(InpFileUtil.SEPARATOR);
                        bw.write(String.valueOf(control.get("REMARK")));
                    }
                    bw.newLine();
                }
            }
            case RULES -> {
                int index = 0;
                for (Map<String, Object> rule : sectList) {
                    for (String s : fields) {
                        if (rule.get(s) != null) {
                            String compStr = String.valueOf(rule.get(s)).toLowerCase();
                            if (index != 0 && compStr.contains("rule")) {
                                if (rule.get("REMARK") != null) {
                                    bw.write(InpFileUtil.SEPARATOR);
                                    bw.write(String.valueOf(rule.get("REMARK")));
                                }
                                bw.newLine();
                            }
                            bw.write(String.valueOf(rule.get(s)));
                            bw.write(InpFileUtil.SPACE);
                        }
                        bw.newLine();
                    }
                    index++;
                }
            }
            case SOURCES, EMITTERS, QUALITY, MIXING, COORDINATES, VERTICES, LABELS, TAGS -> {
                for (Map<String, Object> data : sectList) {
                    for (String field : fields) {
                        writeIfNotNull(bw, data, field);
                        bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                    }
                    bw.newLine();
                }
            }
            case DEMANDS, STATUS, CURVES -> {
                for (Map<String, Object> data : sectList) {
                    for (int i = 0; i < fields.length; i++) {
                        writeIfNotNull(bw, data, fields[i]);
                        bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                        if (i == fields.length - 1 && data.get("REMARK") != null) {
                            bw.write(String.valueOf(data.get("REMARK")));
                        }
                    }
                    bw.newLine();
                }
            }
            case PATTERNS -> {
                String patternId = "";
                int idx = 0;
                for (Map<String, Object> pattern : sectList) {
                    if (idx != 0 && !patternId.equals(String.valueOf(pattern.get("ID")))) {
                        bw.write(InpFileUtil.SEPARATOR);
                        bw.newLine();
                    }
                    for (String s : fields) {
                        if (pattern.get(s) != null) {
                            if (s.equals("MULTIPLIERS")) {
                                String multiplier = String.valueOf(pattern.get(s));
                                multiplier = multiplier.replaceAll("[|]", "\t");
                                bw.write(multiplier);
                            } else {
                                bw.write(String.valueOf(pattern.get(s)));
                            }
                        }
                        bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                    }
                    bw.newLine();
                    patternId = String.valueOf(pattern.get("ID"));
                    idx++;
                }
                bw.write(InpFileUtil.SEPARATOR);
                bw.newLine();
            }
            case ENERGY, REPORT, TIMES, OPTIONS, REACTIONS -> {
                for (Map<String, Object> data : sectList) {
                    writeUnderscoreFields(bw, fields, data);
                }
            }
            case REACTIONS_PT -> {
                for (Map<String, Object> reaction : sectList) {
                    Set<String> reactionKeySet = reaction.keySet();
                    reactionKeySet.remove("SECT_TYPE");
                    for (String key : reactionKeySet) {
                        bw.write(String.valueOf(reaction.get(key)));
                        bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                    }
                    bw.newLine();
                }
            }
            case BACKDROP -> {
                for (Map<String, Object> data : sectList) {
                    for (String s : fields) {
                        if (data.get(s) != null) {
                            bw.write(s);
                            bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                            bw.write(String.valueOf(data.get(s)));
                            bw.newLine();
                        }
                    }
                }
            }
        }
    }

    private static void writeUnderscoreFields(BufferedWriter bw, String[] fields, Map<String, Object> data) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            String[] parseStrArr = fields[i].split("_");
            int splitSize = parseStrArr.length;
            StringBuilder sb = new StringBuilder();
            for (String s : parseStrArr) {
                sb.append(StringUtil.convertStartLetterToUpper(s));
                if (splitSize > 1) sb.append(InpFileUtil.SPACE);
            }
            String parseStr = sb.toString();
            if (data.get(fields[i]) != null) {
                bw.write(parseStr);
                bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                bw.write(String.valueOf(data.get(fields[i])));
                bw.newLine();
            }
        }
    }

    private static void writeDelimitedSection (BufferedWriter bw, List<Map<String, Object>> sectList, SectType sectType) throws IOException {
        final String REMARK_KEY = "REMARK";
        String[] fields = sectType.getSectionFields();

        for (Map<String, Object> data : sectList) {
            for (int i = 0; i < fields.length; i++) {
                writeIfNotNull(bw, data, fields[i]);
                bw.write(InpFileUtil.COMPOSE_SEPARATOR);
                if (i == fields.length - 1 && data.get(REMARK_KEY) != null) {
                    bw.write(InpFileUtil.SEPARATOR);
                    bw.write(String.valueOf(data.get(REMARK_KEY)));
                }
            }
            bw.newLine();
        }
    }

    /* 내용물만 writing */
    public static void writeSimpleSection (BufferedWriter bw, List<Map<String, Object>> sectList, String key) throws IOException {
        for (Map<String, Object> data : sectList) {
            String value = String.valueOf(data.getOrDefault(key, ""));
            bw.write(value);
            bw.newLine();
        }
    }

    private static void writeIfNotNull(BufferedWriter bw, Map<String, Object> map, String key) throws IOException {
        if (map.get(key) != null) {
            bw.write(String.valueOf(map.get(key)));
        }
    }
}
