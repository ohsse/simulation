package com.hscmt.common.util;

import com.hscmt.common.enumeration.ByteUnit;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class OperationSystemUtil {
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final OperatingSystem OS_INFO = SYSTEM_INFO.getOperatingSystem();
    private static final CentralProcessor CPU_INFO = SYSTEM_INFO.getHardware().getProcessor();
    private static final GlobalMemory MEMORY_INFO = SYSTEM_INFO.getHardware().getMemory();

    /* 전체 메모리 가져오기 */
    public static String getTotalMemory () {
        return getTotalMemory(ByteUnit.GIGABYTE);
    }

    /* 전체 메모리 가져오기 */
    public static String getTotalMemory(ByteUnit unit) {
        return getTotalMemory(unit, 2);
    }

    /* 전체 메모리 가져오기 */
    public static String getTotalMemory(ByteUnit unit, int precision) {
        return unit.formatBytes(MEMORY_INFO.getTotal(), precision);
    }

    /* 여유 메모리 가져오기 */
    public static String getAvailableMemory () {
        return getAvailableMemory(ByteUnit.GIGABYTE);
    }

    /* 여유 메모리 가져오기 */
    public static String getAvailableMemory (ByteUnit unit) {
        return getAvailableMemory(unit, 2);
    }

    /* 여유 메모리 가져오기 */
    public static String getAvailableMemory (ByteUnit unit, int precision) {
        return unit.formatBytes(MEMORY_INFO.getAvailable(), precision);
    }
    
    /* 사용중 메모리 가져오기 */
    public static String getUsedMemory () {
        return getUsedMemory(ByteUnit.GIGABYTE);
    }

    /* 사용중 메모리 가져오기 */
    public static String getUsedMemory (ByteUnit unit) {
        return getUsedMemory(unit, 2);
    }

    /* 사용중 메모리 가져오기 */
    public static String getUsedMemory (ByteUnit unit, int precision) {
        BigDecimal total = BigDecimal.valueOf(MEMORY_INFO.getTotal());
        BigDecimal available = BigDecimal.valueOf(MEMORY_INFO.getAvailable());
        BigDecimal used = total.subtract(available);
        return unit.formatBytes(used.longValue(), precision);
    }

    /* 메모리 사용율 가져오기 */
    public static String getMemoryUseRate () {
        return getMemoryUseRate(0);
    }

    /* 메모리 사용율 가져오기 */
    public static String getMemoryUseRate (int precision) {
        BigDecimal total = BigDecimal.valueOf(MEMORY_INFO.getTotal());
        BigDecimal available = BigDecimal.valueOf(MEMORY_INFO.getAvailable());
        BigDecimal used = total.subtract(available);
        BigDecimal useRate = used.divide(total, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(100)).setScale(precision, RoundingMode.HALF_UP);
        return useRate + "%";
    }

    /* cpu 이용율 가져오기 */
    public static String getCpuUseRate () {
        return getCpuUsageRate(0);
    }

    /* cpu 이용율 가져오기 */
    public static String getCpuUsageRate (int precision) {
        long[] prevTicks = CPU_INFO.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        double useRate = CPU_INFO.getSystemCpuLoadBetweenTicks(prevTicks);
        prevTicks = CPU_INFO.getSystemCpuLoadTicks();

        return new BigDecimal(useRate * 100).setScale(precision, RoundingMode.HALF_UP) + "%";
    }

    public static String getOperatingSystemName () {
        return OS_INFO.getFamily().toLowerCase();
    }
}
