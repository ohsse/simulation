package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@RequiredArgsConstructor
public enum ByteUnit {
    BYTE(1, "B"),
    KILOBYTE(Math.pow(1024, 1), "KB"),
    MEGABYTE(Math.pow(1024, 2), "MB"),
    GIGABYTE(Math.pow(1024, 3), "GB"),
    TERABYTE(Math.pow(1024, 4), "TB"),
    ;
    private final double factor;
    private final String label;

    public double toBytes(double value) {
        return value * factor;
    }

    public double fromBytes(double value) {
        return value / factor;
    }

    public String format(double value, Integer precision) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.setScale(precision, RoundingMode.HALF_UP).toString();
    }

    /** 주어진 바이트 값에 가장 적합한 단위를 고릅니다. */
    public static ByteUnit bestFit(long bytes) {
        ByteUnit best = BYTE;
        for (ByteUnit u : values()) {
            if (bytes >= u.factor) best = u;
        }
        return best;
    }

    /** 가장 적합한 단위로 자동 표시 (예: 1.23 GB) */
    public static String humanize(long bytes, int precision) {
        ByteUnit u = bestFit(bytes);
        return u.formatBytes(bytes, precision);
    }

    /** 특정 단위로 강제 표시 (예: MB 기준으로 보고 싶을 때) */
    public String formatBytes(long bytes, int precision) {
        double v = fromBytes(bytes); // bytes -> this 단위 값
        return format(v, precision) + " " + getLabel();
    }

    /** precision 기본값(=2) 오버로드 */
    public static String humanize(long bytes) {
        return humanize(bytes, 2);
    }
}
