package com.hscmt.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ExcelUtil {

    /* sheet 및 헤더정보 가져오기 */
    public static Map<String, List<String>> getSheetAndHeaders (String filePath) {

        Map<String, List<String>> sheetAndHeaders = new HashMap<>();

        try (
                FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = FileUtil.getFileExtension(filePath).equals("xls") ? new HSSFWorkbook(fis) : new XSSFWorkbook(fis);
        ) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet.getLastRowNum() == 0) continue;

                Row row = sheet.getRow(0);
                List<String> headers = new ArrayList<>();

                for (int c = 0; c < row.getLastCellNum(); c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        headers.add("NoName["+c+"]");
                    } else {
                        headers.add(getCellValue(row.getCell(c)));
                    }
                }

                sheetAndHeaders.put(sheet.getSheetName(), headers);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        return sheetAndHeaders;
    }

    public static List<Map<String, Object>> read(String filePath) throws Exception {
        String fileExtension = FileUtil.getFileExtension(filePath);

        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        List<String> headers = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = fileExtension.equals("xls") ? new HSSFWorkbook(fis) : new XSSFWorkbook(fis);
        ) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i < sheet.getLastRowNum(); i ++) {
                Row row = sheet.getRow(i);
                Map<String, Object> record = new LinkedHashMap<String, Object>();

                for (int j = 0; j < row.getLastCellNum(); j ++) {
                    Cell cell = row.getCell(j);
                    String value = getCellValue(cell);
                    if (i == 0) {
                        if (!Objects.isNull(value)) headers.add(value);
                    } else {
                        if (j < headers.size()) {
                            record.put(headers.get(j), value);
                        }
                    }
                }
                returnList.add(record);
            }
        }
        return returnList;
    }

    public static List<String> read(String filePath, String sheetName, int headerIndex) throws Exception{

        String fileExtension = FileUtil.getFileExtension(filePath);

        List<String> valueList = new ArrayList<>();


        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = fileExtension.equals("xls") ? new HSSFWorkbook(fis) : new XSSFWorkbook(fis);
        ) {
            /* 해당 시트 */
            Sheet sheet = workbook.getSheet(sheetName);


            for (int i = 0; i < sheet.getLastRowNum(); i ++) {
                Row row = sheet.getRow(i);

                Cell cell = row.getCell(headerIndex);

                if (cell != null) {
                    valueList.add(getCellValue(cell));
                } else {
                    valueList.add("");
                }
            }
        }

        return valueList;
    }


    public static String getCellValue (Cell cell) {

        if (cell == null) return "";

        String resultValue = "";


        switch (cell.getCellType()) {
            case STRING -> resultValue = cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    if (cell.getDateCellValue() != null) {
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        resultValue = sdf.format(date);
                    } else {
                        resultValue = "";
                    }
                } else {
                    resultValue = String.valueOf(cell.getNumericCellValue()).equals("null") ? "" : String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> resultValue = String.valueOf(cell.getBooleanCellValue()).equals("null") ? "" : String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> resultValue = String.valueOf(cell.getCellFormula()).equals("null") ? "" : String.valueOf(cell.getCellFormula());
            default -> resultValue = "";
        }

        return resultValue;
    }
}
