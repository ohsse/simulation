package com.hscmt.common.util;

import com.hscmt.common.enumeration.SectType;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InpParser {
    public static Map<SectType, List<Map<String, Object>>> parseInpToMap(File file) {
        String charset = FileUtil.getFileEncodingCharset(file);
        Map<SectType, List<Map<String, Object>>> resultMap = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            String line;
            SectType currentSectType = null;
            List<Map<String, Object>> sectList = null;
            int idx = 0;
            String bfId = "";

            while ((line = br.readLine()) != null) {
                String comment = "";
                if (line.contains(InpFileUtil.SEPARATOR)) {
                    int index = line.indexOf(InpFileUtil.SEPARATOR);
                    if (index > 0) comment = line.substring(index + 1).trim();
                    line = line.substring(0, index).trim();
                } else {
                    line = line.trim();
                }

                if (line.isEmpty()) continue;

                if (line.startsWith("[") && line.endsWith("]")) {
                    SectType sectType = findSectType(line);
                    if (sectType != null) {
                        if (currentSectType != null && sectList != null) {
                            resultMap.put(currentSectType, sectList);
                        }
                        currentSectType = sectType;
                        sectList = new ArrayList<>();
                        idx = 0;
                        bfId = "";
                    }
                    continue;
                }

                if (currentSectType == null) continue;

                String[] tokens = splitLine(line);
                if (tokens.length == 0) continue;

                Map<String, Object> data = null;
                switch (currentSectType) {
                    case TITLE:
                        data = new LinkedHashMap<>();
                        StringBuilder sbTitle = new StringBuilder();
                        for (String t : tokens) {
                            sbTitle.append(t);
                        }
                        data.put(currentSectType.toString(), sbTitle.toString());
                        break;
                    case JUNCTIONS, RESERVOIRS, TANKS, PIPES, SOURCES, EMITTERS, VALVES, MIXING, COORDINATES:
                        data = createEntry(currentSectType.getSectionFields(), tokens, comment, currentSectType);
                        break;
                    case PUMPS:
                        data = new LinkedHashMap<>();
                        for (int i = 0; i < tokens.length; i++) {
                            if (i <= 2) {
                                data.put(currentSectType.getSectionFields()[i], tokens[i]);
                            } else {
                                data.put("PARAMETERS", data.getOrDefault("PARAMETERS", "") + tokens[i] + InpFileUtil.SPACE);
                            }
                        }
                        data.put("PARAMETERS", ((String) data.get("PARAMETERS")).trim());
                        data.put("REMARK", comment);
                        data.put("SECT_TYPE", currentSectType);
                        break;
                    case CONTROLS:
                    case RULES:
                        data = new LinkedHashMap<>();
                        StringBuilder sbStatement = new StringBuilder();
                        for (String token : tokens) {
                            sbStatement.append(token).append(InpFileUtil.SPACE);
                        }
                        data.put("STATEMENT", sbStatement.toString().trim());
                        data.put("REMARK", comment);
                        data.put("SECT_TYPE", currentSectType);
                        data.put("IDX", idx++);
                        break;
                    case DEMANDS:
                        data = createEntry(currentSectType.getSectionFields(), tokens, comment, currentSectType);
                        data.put("IDX", idx++);
                        break;
                    case QUALITY, STATUS, CURVES :
                        data = createEntry(currentSectType.getSectionFields(), tokens, comment, currentSectType);
                        if (!tokens[0].equals(bfId)) idx = 0;
                        bfId = tokens[0];
                        data.put("IDX", idx++);
                        break;
                    case PATTERNS:
                        data = new LinkedHashMap<>();
                        data.put("ID", tokens[0]);
                        StringBuilder sbMultipliers = new StringBuilder();
                        for (int i = 1; i < tokens.length; i++) {
                            sbMultipliers.append(tokens[i]).append(InpFileUtil.PIPE);
                        }
                        data.put("MULTIPLIERS", sbMultipliers.toString());
                        if (!tokens[0].equals(bfId)) idx = 0;
                        bfId = tokens[0];
                        data.put("IDX", String.valueOf(idx++));
                        break;
                    case ENERGY:
                        data = new LinkedHashMap<>();
                        createFieldData(data, tokens, currentSectType, idx);
                        break;
                    case REACTIONS:
                        data = new LinkedHashMap<>();
                        String type = tokens[0];
                        if (type.equalsIgnoreCase("PIPE") || type.equalsIgnoreCase("TANK")) {
                            currentSectType = SectType.REACTIONS_PT;
                            data = createEntry(currentSectType.getSectionFields(), tokens, "", currentSectType);
                            data.put("SECT_TYPE", SectType.REACTIONS_PT);
                        } else {
                            createFieldData(data, tokens, currentSectType);
                            data.put("SECT_TYPE", currentSectType);
                        }

                        break;
                    case REPORT, OPTIONS, TIMES:
                        data = new LinkedHashMap<>();
                        createFieldData(data, tokens, currentSectType);
                        break;
                    case VERTICES:
                        data = new LinkedHashMap<>();
                        int limit = tokens.length >= 4 ? 3 : tokens.length;
                        for (int i = 0; i < limit; i++) {
                            data.put(currentSectType.getSectionFields()[i], tokens[i]);
                        }
                        if (tokens.length > 0 && !tokens[0].equals(bfId)) idx = 0;
                        if (tokens.length > 0) bfId = tokens[0];
                        data.put("SECT_TYPE", currentSectType);
                        break;
                    case LABELS:
                        data = new LinkedHashMap<>();
                        data.put("ID", idx++);
                        data.put("IDX", idx);
                        data.put("X", tokens[0]);
                        data.put("Y", tokens[1]);
                        StringBuilder sbLabels = new StringBuilder();
                        for (int i = 2; i < tokens.length; i++) {
                            sbLabels.append(tokens[i]).append(InpFileUtil.SPACE);
                        }
                        data.put("LABEL", sbLabels.toString().replace("\"", "").trim());
                        data.put("SECT_TYPE", currentSectType);
                        break;
                    case TAGS:
                        data = new LinkedHashMap<>();
                        data.put("TYPE", tokens[0]);
                        data.put("ID", tokens[1]);
                        StringBuilder sbTags = new StringBuilder();
                        for (int i = 2; i < tokens.length; i++) {
                            sbTags.append(tokens[i]).append(InpFileUtil.SPACE);
                        }
                        data.put("INFO", sbTags.toString().trim());
                        data.put("SECT_TYPE", currentSectType);
                        break;
                    case BACKDROP:
                        data = new LinkedHashMap<>();
                        StringBuilder sbBackdrops = new StringBuilder();
                        for (String token : tokens) {
                            sbBackdrops.append(token).append(InpFileUtil.SPACE);
                        }
                        String backdropsLine = sbBackdrops.toString().toUpperCase();
                        for (String key : currentSectType.getSectionFields()) {
                            String keyMod = key.replace(InpFileUtil.UNDER_SCORE, InpFileUtil.SPACE);
                            if (backdropsLine.contains(keyMod)) {
                                data.put(key, backdropsLine.substring(keyMod.length()).trim());
                            }
                        }
                        if (!data.isEmpty()) {
                            data.put("SECT_TYPE", currentSectType);
                        }
                        break;
                    default:
                        break;
                }

                if (data != null && !data.isEmpty()) {
                    if (sectList == null) sectList = new ArrayList<>();
                    sectList.add(data);
                }
            }

            if (currentSectType != null && sectList != null) {
                resultMap.put(currentSectType, sectList);
            }

        } catch (Exception e) {
            log.error("error message : {}" , e.getMessage());
        }

        return resultMap;
    }


    private static void createFieldData(Map<String, Object> data, String[] tokens, SectType sectType) {
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            builder.append(token).append(InpFileUtil.SPACE);
        }
        String currLine = builder.toString().toUpperCase();
        for (String key : sectType.getSectionFields()) {
            String keyMod = key.replaceAll(InpFileUtil.UNDER_SCORE, InpFileUtil.SPACE);
            if (currLine.contains(keyMod)) {
                data.put(key, currLine.substring(keyMod.length()).trim());
            }
        }
        data.put("SECT_TYPE", sectType.getInpTitle());
    }

    private static void createFieldData(Map<String, Object> data, String[] tokens, SectType sectType, int idx) {
        createFieldData(data, tokens, sectType);
        data.put("IDX", idx++);
    }

    private static Map<String, Object> createEntry(String[] titles, String[] values, String comment, SectType sectType) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (int i = 0; i < values.length && i < titles.length; i++) {
            data.put(titles[i], values[i].replace(InpFileUtil.COMPOSE_SEPARATOR, InpFileUtil.EMPTY).trim());
        }
        if (comment != null && !comment.isEmpty()) {
            data.put("REMARK", comment);
        }
        data.put("SECT_TYPE", sectType);
        return data;
    }

    private static String[] splitLine(String line) {
        return line.trim().split(InpFileUtil.SPLITTER_REGEX);
    }

    private static SectType findSectType(String line) {
        SectType[] sectTypes = SectType.values();

        for (SectType sectType : sectTypes) {
            if (sectType.getInpTitle().equalsIgnoreCase(line)) {
                return sectType;
            }
        }

        return null;
    }
}
