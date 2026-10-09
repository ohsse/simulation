package com.hscmt.common.util;

import org.springframework.http.ContentDisposition;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringUtil {
    /**
     * underscore 포함 문자열 Camel Case 형 적용
     * @param str : camel 케이스로 변경할 문자열
     * @return camel case style String
     */
    public static String convertToCamelCase(String str) {
        /* underscore 포함하지 않을시 원본 문자열 리턴 */
        if(str.indexOf('_') < 0 && Character.isLowerCase(str.charAt(0))) return str;

        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;

        int length = str.length();

        for(int i = 0; i < length; i ++) {
            char currChar = str.charAt(i);
            if (currChar == '_') nextUpper = true;
            else if (nextUpper) {
                sb.append(Character.toUpperCase(currChar));
                nextUpper = false;
            }else sb.append(Character.toLowerCase(currChar));
        }

        return sb.toString();
    }

    /**
     * CamelCase Style 적용 해제
     * @param camelStr : 카멜케이스 적용 문자열
     * @return : underscore 문자열
     */
    public static String revertCamelCase (String camelStr) {
        StringBuilder sb = new StringBuilder();
        String underScore = "_";
        int length = camelStr.length();
        int upperLength = 0;
        for(int i = 0; i < length; i ++) {
            char currChar = camelStr.charAt(i);
            if(Character.isUpperCase(currChar)) {
                upperLength++;
                sb.append(underScore);
            }
            sb.append(Character.toUpperCase(currChar));
        }

        if(upperLength == 0) return camelStr;
        else return sb.toString();
    }

    /**
     * lpad : 왼쪽문자채우기
     * @param str : 원본문자열
     * @param length : 전체문자열 길이
     * @return
     */
    public String lpad (String str, int length) {
        return lpad(str, length, ' ');
    }

    /**
     * lpad : 왼쪽문자채우기
     * @param str : 원본문자열
     * @param length : 전체문자열 길이
     * @param fillChar : 채울문자
     * @return
     */
    public static String lpad (String str, int length, char fillChar) {
        int strLength = str.length();
        /* 원본문자열 길이가 리턴문자열길이보다 크거나 같다면 원본문자열 리턴 */
        if(strLength >= length) {
            return str;
        }

        StringBuffer sb = new StringBuffer(length);

        int diffLength = length - strLength;

        for (int i = 0 ; i < diffLength; i ++) {
            sb.append(fillChar);
        }

        sb.append(str);

        return sb.toString();
    }

    /**
     * lpad : 왼쪽문자열채우기
     * @param str : 원본문자열
     * @param length : 전체문자열길이
     * @param fillStr : 채울문자열
     * @return
     */
    public static String lpad (String str, int length, String fillStr) {
        int strLength = str.length();
        int diffLength = length - strLength;
        int fillLength = fillStr.length();

        if (length <= strLength) return str;

        if (fillLength == 0) throw new IllegalArgumentException("Argument fillStr can't be 0 length String.");

        int cnt = diffLength / fillLength;

        StringBuffer sb = new StringBuffer(length);
        appendStrByCount(fillStr, diffLength, fillLength, cnt, sb);
        sb.append(str);
        return sb.toString();
    }

    /**
     * rpad : 오른쪽문자 채우기
     * @param str : 원본문자열
     * @param length : 총문자열길이
     * @return
     */
    public static String rpad (String str, int length) {
        return rpad(str, length, ' ');
    }

    /**
     * rpad : 오른쪽문자 채우기
     * @param str : 원본문자열
     * @param length : 총문자열길이
     * @param fillChar : 채울문자
     * @return
     */
    public static String rpad (String str, int length, char fillChar) {
        int strLength = str.length();
        if (length <= strLength) {
            return str;
        }

        StringBuffer sb = new StringBuffer(length);

        sb.append(str);

        int diffLength = 0;
        for (int i = 0 ; i < diffLength; i ++) {
            sb.append(fillChar);
        }

        return sb.toString();
    }

    /**
     * rpad : 우측문자열붙히기
     * @param str : 원본문자열
     * @param length : 총문자열길이
     * @param fillStr : 채울문자열
     * @return
     */
    public static String rpad (String str, int length, String fillStr) {
        int strLength = str.length();
        int fillLength = fillStr.length();
        int diffLength = length - strLength;

        if (length <= strLength) return str;

        if (fillLength == 0) throw new IllegalArgumentException("Argument fillStr can't be 0 length String.");

        StringBuffer sb = new StringBuffer(length);
        sb.append(str);

        int strt = strLength % fillLength;
        int end = fillLength - strt <= diffLength ? fillLength : strt + diffLength;

        for (int i = strt ; i < end; i ++) {
            sb.append(fillStr.charAt(i));
        }

        diffLength -= end - strt;
        int cnt = diffLength / fillLength;
        appendStrByCount(fillStr, diffLength, fillLength, cnt, sb);

        return sb.toString();
    }

    /**
     * 빈문자열 대체하기
     * @param str : 원본문자열
     * @param replacement : 대체문자열
     * @return 원문자열 == null ? 대체문자열 : 원본문자열
     */
    public static String nvl (Object str, String replacement) {
        System.out.println(Objects.isNull(str)||str.equals(""));
        return (Objects.isNull(str)||str.equals(""))
                ?
                replacement : String.valueOf(str);
    }

    /**
     * 문자열합치기
     * @param appender 시작문자열
     * @param args 추가문자열
     * @return 합친 문자열
     */
    public static String aggregateStringByAppender (String appender, String... args) {
        StringBuilder returnStr = new StringBuilder();
        for (int i = 0 ; i < args.length; i++) {
            returnStr.append(args[i]);
            if (i < args.length - 1) {
                returnStr.append(appender);
            }
        }
        return returnStr.toString();
    }

    /**
     * Url Encoding 문자열
     * @param targetStr 목표문자열
     * @return Url Encoding 문자열
     */
    public static String getUrlEncodeStr (String targetStr) {
        try {
            return URLEncoder.encode(targetStr, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // 공백 처리
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * [-/: ] 를 포함한 날짜형식 문자열 평문으로 변경
     * @param str 날짜형식 문자열
     * @return 평문문자열
     */
    public static String convertDateStringToPlainString(String str) {
        return str.replaceAll("[-/: ]","");
    }

    /**
     * 문자열 채우기
     * @param fillStr 채울 문자열
     * @param diffLength 남은 수
     * @param fillLength 채울 수
     * @param cnt 현재 수
     * @param sb 최종 문자열
     */
    protected static void appendStrByCount(String fillStr, int diffLength, int fillLength, int cnt, StringBuffer sb) {
        for (int i = 0 ; i < cnt; i ++) {
            sb.append(fillStr);
        }
        cnt = diffLength % fillLength;
        for (int i = 0 ; i < cnt; i ++) {
            sb.append(fillStr.charAt(i));
        }
    }

    /**
     * 문자열에 byte 사이즈 구하기
     * @param str 문자열
     * @param encoding encoding type
     * @return 문자열 byte 사이즈
     */
    public static int getStrByteSize (String str, String encoding) {
        try {
            return str.getBytes(encoding).length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding Error : ", e);
        }
    }

    public static String getSuffix (String str) {
        if (str == null || !str.contains("_")) {
            return str; // 언더스코어가 없는 경우 원본 반환
        }
        String[] parts = str.split("_");
        return parts[parts.length - 1];
    }

    public static String getPrefix (String str) {
        if (str == null || !str.contains("_")) {
            return str;
        }
        String[] parts = str.split("_");
        return parts[0];
    }

    public static String convertStartLetterToUpper(String str) {
        if (nvl(str, "").isEmpty()) return str;

        StringBuffer sb = new StringBuffer(str.substring(0,1).toUpperCase());
        sb.append(str.substring(1).toLowerCase());

        return sb.toString();
    }

    public static String contentDispositionFileName (String fileName) {
        return ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8) // filename / filename* 모두 생성
                .build()
                .toString();
    }
}
