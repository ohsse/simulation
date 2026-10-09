package com.hscmt.common.util;

import com.hscmt.common.enumeration.SectType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InpCombiner {

    public static Map<String, Object> combineInpModel (Map<SectType, List<Map<String, Object>>> inpMap) {
        Iterator<SectType> itor = inpMap.keySet().iterator();
        Map<String, Object> resultMap = new LinkedHashMap<>();
        Map<String, Object> nodeLayer = new LinkedHashMap<>();
        Map<String, Object> linkLayer = new LinkedHashMap<>();

        while (itor.hasNext()) {
            SectType sectType = itor.next();
            List<Map<String, Object>> sectList = inpMap.get(sectType);
            resultMap.put(sectType.name(), sectList);
            switch (sectType) {
                case JUNCTIONS, RESERVOIRS, TANKS -> {
                    sectList.forEach(node -> combineNode(inpMap, node));
                    nodeLayer.put(sectType.name(), GeometryUtil.listToGeoJSON(sectList, "gmtrVal"));
                }
                case PIPES,PUMPS,VALVES -> {
                    sectList.forEach(link -> combineLink(inpMap, link));
                    linkLayer.put(sectType.name(), GeometryUtil.listToGeoJSON(sectList, "gmtrVal"));
                }
                case LABELS ->
                {
                    sectList.forEach(label -> {
                        double coordX = Double.parseDouble(String.valueOf(label.get("X")));
                        double coordY = Double.parseDouble(String.valueOf(label.get("Y")));
                        label.put("gmtrVal", GeometryUtil.getWktPoint(coordX, coordY));
                    });
                    nodeLayer.put(sectType.name(), GeometryUtil.listToGeoJSON(sectList,"gmtrVal"));
                }

                case PATTERNS -> resultMap.put(sectType.name(), combinePatterns(sectList));
                case OPTIONS -> resultMap.put(sectType.name(), combineOptions(inpMap, sectList));
            }
        }

        resultMap.put("nodeLayer", nodeLayer);
        resultMap.put("linkLayer", linkLayer);

        return resultMap;
    }

    private static void combineNode (Map<SectType, List<Map<String, Object>>> inpMap, Map<String, Object> node) {
        String nodeId = String.valueOf(node.get("ID"));

        Map<String, Object> coord = inpMap.getOrDefault(SectType.COORDINATES, Collections.emptyList())
                .stream()
                .filter(c -> nodeId.equals(String.valueOf(c.getOrDefault("ID", c.get("NODE")))))
                .findFirst()
                .orElse(null);

        if (coord != null) {
            double x = Double.parseDouble(String.valueOf(coord.get("X")));
            double y = Double.parseDouble(String.valueOf(coord.get("Y")));
            node.put("X", x);
            node.put("Y", y);
            node.put("gmtrVal", GeometryUtil.getWktPoint(x, y));
        }
        List<Map<String, Object>> demands = inpMap.get(SectType.DEMANDS);
        List<Map<String, Object>> emitters = inpMap.get(SectType.EMITTERS);
        List<Map<String, Object>> qualities = inpMap.get(SectType.QUALITY);
        List<Map<String, Object>> sources = inpMap.get(SectType.SOURCES);
        List<Map<String, Object>> tags = inpMap.get(SectType.TAGS);
        List<Map<String, Object>> mixings = inpMap.get(SectType.MIXING);
        List<Map<String, Object>> reactions = inpMap.get(SectType.REACTIONS);

        /* 태그관련 */
        if (tags != null) {
            Map<String, Object> tag = tags.stream()
                    .filter(x -> x.get("ID").equals(node.get("ID")) && x.get("TYPE").equals(InpFileUtil.Type.NODE))
                    .findFirst()
                    .orElse(new HashMap<String, Object> ());

            if (tag.containsKey("INFO")) {
                node.put("INFO", tag.get("INFO"));
            }
        }

        /* 수질관련 */
        if (qualities != null) {
            Map<String, Object> quality = qualities.stream()
                    .filter(x -> x.get("ID").equals(node.get("ID")))
                    .findFirst()
                    .orElse(new HashMap<String, Object> ());

            node.put("INITQUAL", quality.get("INITQUAL"));
        }
        /* 에미터 */
        if (emitters != null) {
            Map<String, Object> emmiter = emitters.stream()
                    .filter(x -> x.get("ID").equals(node.get("ID")))
                    .findFirst()
                    .orElse(new HashMap<String, Object> ());

            node.put("COEFFICIENT", emmiter.get("COEFFICIENT"));
        }
        /* 혼화 */
        if (mixings != null) {
            Map<String, Object> mixing = mixings.stream()
                    .filter(x -> x.get("ID").equals(node.get("ID")))
                    .findFirst()
                    .orElse(new HashMap<String, Object>());

            node.put("MODEL", mixing.get("MODEL"));
            node.put("FRACTION", mixing.get("FRACTION"));
        }

        /* 반응 */
        if (reactions != null && node.get("SECT_TYPE") == SectType.TANKS) {
            List<Map<String, Object>> tempReactions = reactions.stream()
                    .filter(x -> x.get("PIPE/TANK") != null)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), c -> !c.isEmpty()?c:null));

            if (tempReactions != null) {
                Map<String, Object> reaction = tempReactions.stream()
                        .filter(x -> x.get("PIPE/TANK").equals(node.get("ID")))
                        .findFirst()
                        .orElse(new HashMap<String, Object>());
                node.put("COEFFICIENT", reaction.get("COEFFICIENT"));
                node.put("TYPE", reaction.get("TYPE"));
                node.put("PIPE/TANK", node.get("ID"));
            }
        }

        /* 수요 */
        if (demands != null && node.get("SECT_TYPE") == SectType.JUNCTIONS) {
            List<Map<String, Object>> node_demands = demands.stream()
                    .filter(x -> x.get("ID").equals(node.get("ID")))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), c -> !c.isEmpty()?c:null));

            if (node_demands == null) {
                node.put("DEVIDED_DEMAND_COUNT", 1);
            } else {
                node.put("DEMANDS_INFO", node_demands);
                node.put("DEVIDED_DEMAND_COUNT", node_demands.size());
            }
        } else if (demands == null && node.get("SECT_TYPE") == SectType.JUNCTIONS) node.put("DEVIDED_DEMAND_COUNT", 1);

        /* 소스 */
        if (sources != null) {
            Map<String, Object> source = sources.stream()
                    .filter(x -> x.get("ID").equals(node.get("ID")))
                    .findFirst()
                    .orElse(new HashMap<String, Object>());
            node.put("SOURCE_TYPE", source.get("TYPE"));
            node.put("SOURCE_PATTERN_ID", source.get("PATTERN_ID"));
            node.put("SOURCE_QUALITY", source.get("QUALITY"));
        }
    }

    private static void combineLink (Map<SectType, List<Map<String, Object>>> inpMap, Map<String, Object> link) {
        String from = String.valueOf(link.get("NODE1"));
        String to = String.valueOf(link.get("NODE2"));

        List<Map<String, Object>> coords = inpMap.getOrDefault(SectType.COORDINATES, null);
        List<Map<String, Object>> vertices = inpMap.getOrDefault(SectType.VERTICES, null);

        if (coords != null && !coords.isEmpty()) {
            Map<String, Object> fromCoord = coords.stream()
                    .filter(c -> from.equals(String.valueOf(c.get("ID"))))
                    .findFirst()
                    .orElse(null);
            Map<String, Object> toCoord = coords.stream()
                    .filter(c -> to.equals(String.valueOf(c.get("ID"))))
                    .findFirst()
                    .orElse(null);

            List<double[]> lines = new ArrayList<>();
            if (fromCoord != null) lines.add(new double[]{Double.parseDouble(fromCoord.get("X").toString()), Double.parseDouble(fromCoord.get("Y").toString())});
            if (vertices != null) {
                vertices.stream()
                        .filter(v -> link.get("ID").equals(v.get("ID")))
                        .forEach(v -> lines.add(new double[]{Double.parseDouble(v.get("X").toString()), Double.parseDouble(v.get("Y").toString())}));
            }
            if (toCoord != null) lines.add(new double[]{Double.parseDouble(toCoord.get("X").toString()), Double.parseDouble(toCoord.get("Y").toString())});

            Double [] x = new Double [lines.size()];
            Double [] y = new Double [lines.size()];

            for (int i = 0; i < lines.size(); i ++) {
                x[i] = lines.get(i)[0];
                y[i] = lines.get(i)[1];
            }

            link.put("gmtrVal", GeometryUtil.getWktLineString(x, y));
        }

        /* 태그 꼬리표 */
        List<Map<String, Object>> tags = inpMap.get(SectType.TAGS);
        List<Map<String, Object>> status = inpMap.get(SectType.STATUS);
        List<Map<String, Object>> energy = inpMap.get(SectType.ENERGY);

        /* 태그관련 */
        if (tags != null) {
            tags.stream()
                    .filter(x -> x.get("TYPE") == InpFileUtil.Type.LINK && x.get("ID").equals(link.get("ID")))
                    .findFirst().ifPresent(tag -> link.put("INFO", tag.get("INFO")));
        }

        /* 상태 */
        if (status != null) {
            Map<String, Object> statusMap = status.stream()
                    .filter(x -> x.get("ID").equals(link.get("ID")))
                    .filter(x -> String.valueOf(x.get("STATUS_SETTING")).matches("^[a-z|A-Z|ㄱ-ㅎ|가-힣| ]*$"))
                    .findFirst()
                    .orElse(null);

            if (statusMap != null) {
                if (link.get("SECT_TYPE") == SectType.VALVES) {
                    link.put("FIXED_STATUS", String.valueOf(statusMap.get("STATUS_SETTING")).toUpperCase());
                } else {
                    link.put("STATUS_SETTING", String.valueOf(statusMap.get("STATUS_SETTING")).toUpperCase());
                }

            }
        }

        if (link.get("SECT_TYPE") == SectType.PUMPS) {

            if (energy != null ) {
                List<Map<String, Object>> pumpEnergyList = energy.stream()
                        .filter(x -> x.containsKey("PUMP"))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), c -> !c.isEmpty()?c:null));

                if (pumpEnergyList != null) {
                    for (Map<String, Object> stringObjectMap : pumpEnergyList) {
                        String pumpStr = String.valueOf(stringObjectMap.get("PUMP"));

                        String pumpId = pumpStr.split(InpFileUtil.SPACE)[0];
                        String type = "ENERGY_" + pumpStr.split(InpFileUtil.SPACE)[1].toUpperCase();
                        String value = pumpStr.split(InpFileUtil.SPACE)[2];

                        if (pumpId.equals(link.get("ID"))) {
                            link.put(type, value);
                        }
                    }
                }
            }

            if (link.get("PARAMETERS") != null
                    && !String.valueOf(link.get("PARAMETERS")).equals("null")
                    && !String.valueOf(link.get("PARAMETERS")).isEmpty()
            ) {
                String [] parameters = String.valueOf(link.get("PARAMETERS")).split(InpFileUtil.SPACE);
                int plength = parameters.length;

                if (plength > 1) link.put("PUMP_CURVE", parameters[1]);
                if (plength > 3) link.put("SPEED", parameters[3]);
                if (plength > 5) link.put("PATTERN", parameters[5]);
            }
        }

        /* 반응 */
        List<Map<String, Object>> reactions = inpMap.get(SectType.REACTIONS);
        if (reactions != null && link.get("SECT_TYPE") == SectType.PIPES) {
            List<Map<String, Object>> tempReactions = reactions.stream()
                    .filter(x -> x.get("PIPE/TANK") != null)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), c -> !c.isEmpty()?c:null));

            if (tempReactions != null) {
                Map<String, Object> reaction = tempReactions.stream()
                        .filter(x -> x.get("PIPE/TANK").equals(link.get("ID")))
                        .findFirst()
                        .orElse(new HashMap<String, Object>());
                link.put("COEFFICIENT", reaction.get("COEFFICIENT"));
                link.put("TYPE", reaction.get("TYPE"));
                link.put("ID", link.get("ID"));
            }
        }
    }


    private static List<Map<String, Object>> combinePatterns (List<Map<String, Object>> sectList) {
        Map<String, Map<String, Object>> patternsMap = new LinkedHashMap<>();
        Set<String> idSet = sectList.stream()
                .map(x -> String.valueOf(x.get("ID")))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String patternId : idSet) {
            Map<String, Object> patternMap = new LinkedHashMap<>();
            List<Double> multipliers = new ArrayList<>();

            List<Map<String, Object>> currentPatternList = sectList.stream()
                    .filter(x -> x.get("ID").equals(patternId))
                    .collect(Collectors.toList());

            for (Map<String, Object> pattern : currentPatternList) {
                String[] multiplierArr = String.valueOf(pattern.get("MULTIPLIERS")).split("\\|");
                for (String m : multiplierArr) {
                    multipliers.add(Double.parseDouble(m));
                }
            }
            patternMap.put("ID", patternId);
            patternMap.put("MULTIPLIERS", multipliers);
            patternsMap.put(patternId, patternMap);
        }
        return new ArrayList<>(patternsMap.values());
    }

    private static Map<String, Object> combineOptions(Map<SectType, List<Map<String, Object>>> inpMap, List<Map<String, Object>> sectList) {
        Map<String, Object> option = new LinkedHashMap<>();
        Map<String, Object> math = new LinkedHashMap<>();
        Map<String, Object> quality = new LinkedHashMap<>();
        Map<String, Object> reaction = new LinkedHashMap<>();
        Map<String, Object> time = new LinkedHashMap<>();
        Map<String, Object> energy = new LinkedHashMap<>();

        option.put("MATH", math);
        option.put("QUALITY", quality);
        option.put("REACTION", reaction);
        option.put("TIME", time);
        option.put("ENERGY", energy);

        for (String mathTitle : InpFileUtil.MATH_OPTION_TITLE) {
            sectList.stream()
                    .filter(x -> x.containsKey(mathTitle))
                    .findFirst()
                    .ifPresent(x -> math.put(mathTitle, x.get(mathTitle)));
        }

        Map<String, Object> qualityMap = sectList.stream()
                .filter(x -> x.containsKey("QUALITY"))
                .findFirst()
                .orElse(null);

        if (qualityMap != null) {
            for (String qualTitle : InpFileUtil.QUALITY_OPTION_TITLE) {
                if ("PARAMETER".equals(qualTitle)) {
                    String parameter = String.valueOf(qualityMap.get("QUALITY"));
                    if (!"null".equals(parameter)) {
                        String[] splitParam = parameter.split(InpFileUtil.SPACE);
                        if (splitParam.length >= 1) quality.put(qualTitle, splitParam[0]);
                        if (splitParam.length >= 2) {
                            String qualType = splitParam[0];
                            if ("trace".equalsIgnoreCase(qualType)) {
                                quality.put("TRACE_NODE", splitParam[1]);
                            } else {
                                quality.put("MASS_UNITS", splitParam[1]);
                            }
                        }
                    }
                } else if ("DIFFUSIVITY".equals(qualTitle) || "TOLERANCE".equals(qualTitle)) {
                    sectList.stream()
                            .filter(x -> x.containsKey(qualTitle))
                            .findFirst()
                            .ifPresent(qmap -> quality.put(qualTitle, qmap.get(qualTitle)));
                }
            }
        }

        List<Map<String, Object>> reactions = inpMap.get(SectType.REACTIONS);
        if (reactions != null) {
            for (String r : SectType.REACTIONS.getSectionFields()) {
                reactions.stream()
                        .filter(x -> x.containsKey(r))
                        .findFirst()
                        .ifPresent(rmap -> reaction.put(r, rmap.get(r)));
            }
        }

        List<Map<String, Object>> times = inpMap.get(SectType.TIMES);
        if (times != null) {
            for (String t : SectType.TIMES.getSectionFields()) {
                times.stream()
                        .filter(x -> x.containsKey(t))
                        .findFirst()
                        .ifPresent(tmap -> time.put(t, tmap.get(t)));
            }
        }

        List<Map<String, Object>> orgEnergies = inpMap.get(SectType.ENERGY);
        if (orgEnergies != null) {
            for (String e : SectType.ENERGY.getSectionFields()) {
                if ("PUMP".equals(e)) {
                    List<Map<String, Object>> elist = orgEnergies.stream()
                            .filter(x -> x.containsKey(e))
                            .collect(Collectors.toList());
                    if (!elist.isEmpty()) {
                        energy.put("PUMPS", elist);
                    }
                } else {
                    Map<String, Object> emap = orgEnergies.stream()
                            .filter(x -> x.containsKey(e))
                            .findFirst()
                            .orElse(null);
                    if (emap != null) {
                        for (String key : emap.keySet()) {
                            if (!key.contains("SECT_TYPE") && !key.contains("IDX")) {
                                energy.put(key, emap.get(key));
                            }
                        }
                    }
                }
            }
        }
        return option;
    }
}
