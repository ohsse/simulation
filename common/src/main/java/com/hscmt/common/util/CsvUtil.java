package com.hscmt.common.util;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Slf4j
public class CsvUtil {

    public static List<String> getHeaders (String filePath) {
        List<String> headers = new ArrayList<>();
        try (
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                CSVReader csvReader = new CSVReader(br);
        ){

            int readLine = 0;
            while (readLine == 0) {
                if (readLine == 0) {
                    headers.addAll(Arrays.asList(csvReader.readNext()));
                }
                readLine++;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        return headers;
    }

    public static List<Map<String, Object>> read(String filePath) throws Exception {
        try (
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                CSVReader csvReader = new CSVReader(br);
        ) {
            List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
            List<String> headers = new ArrayList<>();

            String[] nextLine;

            int readLine = 0;
            while ((nextLine = csvReader.readNext()) != null){
                if (readLine == 0){
                    headers.addAll(Arrays.asList(nextLine));
                } else {
                    Map<String, Object> record = new LinkedHashMap<String, Object>();

                    for (int i = 0; i < nextLine.length; i ++) {
                        record.put(headers.get(i), nextLine[i]);
                    }
                    returnList.add(record);
                }

                readLine++;
            }

            return returnList;
        }
    }

    public static List<String> read (String filePath, int headerIndex) throws Exception{
        List<String> valueList = new ArrayList<>();
        try (
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                CSVReader csvReader = new CSVReader(br);
        ) {
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null){
                valueList.add(nextLine[headerIndex]);
            }
        }
        return valueList;
    }

}
