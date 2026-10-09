package com.hscmt.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
public class RptFileReader {

    private final static String NODE_TITLE = "Node Results";
    private final static String LINK_TITLE = "Link Results";
    private final static String SPACE_STR = "\\s++";
    private final static int HOURS_INDEX = 4;
    private final static String HEADER_SPLIT_STR = "---";

    public enum ReportTarget {
        Node,
        Link,
    }
    public static Map<String, Object> read (String filePath) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();

        List<Map<String, Object>> nodeResults = new ArrayList<>();
        List<Map<String, Object>> linkResults = new ArrayList<>();
        Set<String> timeSet = new LinkedHashSet<>();

        List<String> linkHeaders = new ArrayList<>();
        List<String> nodeHeaders = new ArrayList<>();

        boolean isContent = false;
        boolean isLink = false;

        int headerIndex = 0;
        String reportTime = "";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "EUC-KR"))) {
            String line;
            while ((line = br.readLine()) != null) {
                /* Report Content 시작지점 확인 */
                if (line.contains(NODE_TITLE) || line.contains(LINK_TITLE)) {
                    isContent = true;
                    List<String> splitLine = Arrays.asList(line.split(SPACE_STR));

                    /* Duration Time 존재 확인 */
                    if (splitLine.contains("at") && splitLine.contains("hrs:")) {
                        reportTime = splitLine.get(HOURS_INDEX);
                        timeSet.add(reportTime);
                    }
                }

                /* 컨텐츠 헤더 시작부분 확인 */
                if (isContent && line.contains(HEADER_SPLIT_STR)) {
                    headerIndex++;
                    continue;
                }

                boolean isLinePossible = !line.trim().isEmpty() && !line.trim().startsWith(" ");

                /* 헤더데이터 적재 */
                if (headerIndex == 1) {
//                    System.out.println(line);
                    if (isLinePossible) {
                        /* 노드 컨텐츠라면 노드헤더 저장 */
                        if (line.contains("Demand")) {
                            if (nodeHeaders.isEmpty()) {
                                nodeHeaders.addFirst("Node");
                                nodeHeaders.addAll(Arrays.asList(line.trim().split(SPACE_STR)));
                            }
                            isLink = false;
                        }
                        /* 링크 컨텐츠라면 링크헤더 저장 */
                        if (line.contains("Flow")) {
                            if (linkHeaders.isEmpty()) {
                                linkHeaders.addFirst("Link");
                                linkHeaders.addAll(Arrays.asList(line.trim().split(SPACE_STR)));
                            }
                            isLink = true;
                        }

                        if (!line.contains(ReportTarget.Node.name()) || !line.contains(ReportTarget.Link.name())) {
                            headerIndex++;
                        }
                    }
                } else if (isContent && headerIndex >=3) {
                    /* 컨텐츠 데이터 존재시 */
                    if (isLinePossible) {
                        /* 값 배열 */
                        String[] values = line.trim().split(SPACE_STR);
                        /* 저장할 레코드 객체 */
                        Map<String, Object> record = new LinkedHashMap<>();
                        /* 링크 결과 추가 */
                        if (isLink) {
                            if (!reportTime.isEmpty()) {
                                record.put("reportTime", reportTime);
                            } else {
                                record.put("reportTime", "0:00:00");
                            }
                            for (int i = 0; i < linkHeaders.size(); i++) {
                                record.put(convertLinkHeaderToLinkFields(linkHeaders.get(i)), values[i]);
                            }
                            linkResults.add(record);
                        }
                        /* 노드 결과 추가 */
                        else {
                            if (!reportTime.isEmpty()) {
                                record.put("reportTime", reportTime);
                            } else {
                                record.put("reportTime", "0:00:00");
                            }
                            for (int i = 0; i < nodeHeaders.size(); i++) {
                                record.put(convertNodeHeaderToNodeFields(nodeHeaders.get(i)), values[i]);
                            }
                            nodeResults.add(record);
                        }

                    } else {
                        headerIndex = 0;
                    }
                }
            }
        }

        if (timeSet.isEmpty()) timeSet.add("0:00:00");

        resultMap.put("nodeResults", nodeResults);
        resultMap.put("linkResults", linkResults);
        resultMap.put("reportTimes", new ArrayList<>(timeSet));

        return resultMap;
    }

    protected static String convertNodeHeaderToNodeFields (String str) {
        return switch (str) {
            case "Node" -> "id";
            case "Demand" -> "demand";
            case "Head" -> "head";
            case "Pressure" -> "pressure";
            case "Quality" -> "quality";
            default -> "";
        };
    }

    protected static String convertLinkHeaderToLinkFields (String str) {
        return switch (str) {
            case "Link" -> "id";
            case "Flow" -> "flow";
            case "Velocity" -> "velocity";
            case "Headloss" -> "headloss";
            case "Status" -> "status";
            case "Quality" -> "quality";
            default -> "";
        };
    }
}
