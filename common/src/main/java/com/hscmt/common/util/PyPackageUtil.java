package com.hscmt.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PyPackageUtil {

    public static final Pattern PYTHON_WHEEL_FILE_PATTERN = Pattern.compile(
            "^([\\w\\d\\-\\.]+)-([\\w\\d\\.]+)-([\\w\\d\\.]+)-([\\w\\d\\.]+)-([\\w\\d_\\.]+)\\.whl$"
    );

    /* 라이브러리 패키지 파이썬 패키지명 태그 가져오기 */
    public static String getPackageNameTag (String wheelFileName) {
        Matcher matcher = getMatcher(wheelFileName);
        return matcher.matches() ? matcher.group(1) : null;
    }

    /* 라이브러리 패키지 파이썬 패키지 버전 태그 가져오기 */
    public static String getPackageVersionTag (String wheelFileName) {
        Matcher matcher = getMatcher(wheelFileName);
        return matcher.matches() ? matcher.group(2) : null;
    }

    /* 라이브러리 패키지 파이썬 버전 태그 가져오기 */
    public static String getPythonVersionTag (String wheelFileName) {
        Matcher matcher = getMatcher(wheelFileName);
        return matcher.matches() ? matcher.group(3) : null;
    }

    /* 라이브러리 패키지 파이썬 os 태그 가져오기 */
    public static String getOsTag (String wheelFileName) {
        Matcher matcher = getMatcher(wheelFileName);
        return matcher.matches() ? matcher.group(5) : null;
    }

    public static boolean isAbleSelectedPythonVersion (String wheelFileName, String selectedPythonVersion) {
        String[] versionParts = selectedPythonVersion.split("\\.");
        if (versionParts.length < 2) {
            throw new RuntimeException("python version format error");
        }

        String convertPythonVersion = "cp" + versionParts[0] + versionParts[1];
        String pythonVersionTag = getPythonVersionTag(wheelFileName);

        if (!isOsAble(wheelFileName)){
            return false;
        }

        if (!(pythonVersionTag.startsWith("py") && pythonVersionTag.contains(versionParts[0])) && !pythonVersionTag.equals(convertPythonVersion) ) {
            return false;
        }

        return true;
    }

    public static boolean isMatched (String wheelFileName) {
        Matcher matcher = getMatcher(wheelFileName);
        return matcher.matches();
    }

    protected static Matcher getMatcher (String wheelFileName) {
        return PYTHON_WHEEL_FILE_PATTERN.matcher(wheelFileName);
    }

    protected static boolean isOsAble (String wheelFileName) {
        String osTag = getOsTag(wheelFileName).toLowerCase();
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            os = os.substring(0, 3);
        }

        return osTag.contains(os) || osTag.equals("any");
    }
}
