package com.hscmt.common.util;


import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.common.enumeration.SectType;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
public class InpFileUtil {

    /* 속성별 정의 */
    /* 수리옵션 목록 */
    public static final String[] MATH_OPTION_TITLE = {
            "UNITS","HEADLOSS","SPECIFIC_GRAVITY","VISCOSITY","TRIALS","ACCURACY","UNBALANCED","PATTERN","DEMAND_MULTIPLIER","EMITTER_EXPONENT"
    };
    /* 수질옵션 목록 */
    public static final String[] QUALITY_OPTION_TITLE = {
            "PARAMETER", "MASS_UNITS", "TRACE_NODE", "DIFFUSIVITY", "TOLERANCE"
    };
    
    /* 파일 read, write 참조 */
    public static final String SEPARATOR  = ";";
    public static final String COMPOSE_SEPARATOR = "\t";
    public final static String SPLITTER_REGEX = "[ \t]+";
    public static final String SPACE = " ";
    public static final String PIPE = "|";
    public static final String UNDER_SCORE = "_";
    public static final String EMPTY = "";

    /* INP Map TO INP 파일 */
    public static void composeMapToInpFile (Map<SectType, List<Map<String, Object>>> inpMap, String outputPath, String charset) {
        composeMapToInpFile(inpMap, new File(outputPath), charset);
    }
    /* INP Map TO INP 파일 */
    public static void composeMapToInpFile (Map<SectType, List<Map<String, Object>>> inpMap, File outputFile, String charset){
        InpComposer.composeMapToInpFile(inpMap, outputFile, charset);
    }
    /* INP 파일 To INP Map */
    public static Map<SectType, List<Map<String, Object>>> parseInpToMap(String filePath) {
        return parseInpToMap(new File(filePath));
    }
    /* INP 파일 To INP Map */
    public static Map<SectType, List<Map<String, Object>>> parseInpToMap(File inpFile) {
        return InpParser.parseInpToMap(inpFile);
    }
    /* Inp Map 관망해석용 Map으로 combine */
    public static Map<String, Object> combineInpModel (Map<SectType, List<Map<String, Object>>> inpMap) {
        return InpCombiner.combineInpModel(inpMap);
    }

    public static Map<String, Object> combineInpModel (File file) {
        return combineInpModel(parseInpToMap(file.getAbsolutePath()));
    }

    /* Inp Map 좌표 변환 */
    public static Map<SectType, List<Map<String, Object>>> transformCoordinate (Map<SectType, List<Map<String, Object>>> inpMap, CrsyType fromEpsg, SectType sectType) {
        if (!(sectType == SectType.COORDINATES || sectType == SectType.VERTICES || sectType == SectType.LABELS)) {
            return inpMap;
        }
        List<Map<String, Object>> list = inpMap.get(sectType);
        if (list == null || list.isEmpty()) {
            return inpMap;
        }
        for (Map<String, Object> coordinate : list) {
            Object xObj = coordinate.get("X");
            Object yObj = coordinate.get("Y");
            if (xObj != null && yObj != null) {
                double x = Double.parseDouble(xObj.toString());
                double y = Double.parseDouble(yObj.toString());
                if (fromEpsg != CrsyType.EPSG5186) {
                    ProjCoordinate transCoodinate = GeometryUtil.transformPointByXy(x, y, fromEpsg, CrsyType.EPSG5186);
                    coordinate.put("X", transCoodinate.x);
                    coordinate.put("Y", transCoodinate.y);
                } else {
                    coordinate.put("X", x);
                    coordinate.put("Y", y);
                }
            }
        }
        inpMap.put(sectType, list);
        return inpMap;
    }

    public enum Type {
        NODE, LINK
    }
}
