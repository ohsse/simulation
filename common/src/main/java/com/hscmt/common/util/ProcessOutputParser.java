package com.hscmt.common.util;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessOutputParser {

    public static List<Map<String, String>> parseKeyValue (String resultStr, String[] keys) {
        List<Map<String, String>> resultList = new ArrayList<>();

        String[] lines = resultStr.split("\n");

        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isBlank()) {
                String [] parts = line.split("\\s+");
                if (parts.length == keys.length) {
                    Map<String, String> resultMap = new LinkedHashMap<>();
                    for (int j = 0; j < keys.length; j++) {
                        resultMap.put(keys[j], parts[j]);
                    }
                    resultList.add(resultMap);
                }
            }
        }

        return resultList;
    }
}
