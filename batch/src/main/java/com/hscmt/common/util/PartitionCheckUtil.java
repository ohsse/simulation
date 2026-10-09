package com.hscmt.common.util;

import com.hscmt.simulation.partition.dto.PartitionRangeDto;
import com.hscmt.simulation.partition.rule.PartitionRule;
import com.hscmt.simulation.partition.rule.type.HashPartition;
import com.hscmt.simulation.partition.rule.type.RangePartition;
import com.hscmt.simulation.partition.spec.type.PartitionRangeType;
import com.hscmt.simulation.partition.spec.type.RangeFieldType;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PartitionCheckUtil {

    /* 파티션 생성 필요 여부 */
    public static boolean isNeedCreateRangePartition(PartitionRule partitionRule, String lastPartitionName) {
        if (isRangePartition(partitionRule)) {
            RangePartition rangePartition = (RangePartition) partitionRule;
            PartitionRangeType rangeType = rangePartition.getPartitionRangeType();
            // 마지막 파티션 이름 기준 날짜
            String lastPartitionNamePart = getPartitionPartName(lastPartitionName);

            LocalDate lastPartitionDate = switch (rangeType) {
                case MONTHLY -> getLocalDateByString(lastPartitionNamePart);
                case QUARTERLY -> {
                    int year = Integer.parseInt(lastPartitionNamePart.substring(0, 4));
                    int q = Integer.parseInt(lastPartitionNamePart.substring(5, 6));
                    int month = (q - 1) * 3 + 1;
                    yield LocalDate.of(year, month, 1);
                }
                case HALF_YEARLY -> {
                    int year = Integer.parseInt(lastPartitionNamePart.substring(0, 4));
                    int h = Integer.parseInt(lastPartitionNamePart.substring(5, 6));
                    int month = (h - 1) * 6 + 1;
                    yield LocalDate.of(year, month, 1);
                }
                case YEARLY -> {
                    int year = Integer.parseInt(lastPartitionNamePart.substring(0, 4));
                    yield LocalDate.of(year, 1, 1);
                }
            };

            // 마지막 파티션의 끝나는 시점
            LocalDate nextPartitionDate = lastPartitionDate.plus(rangeType.getPeriod(), rangeType.getUnit());

            // 비교일자를 하루 미리 준비 (배치용)
            LocalDate compareDate = LocalDate.now().plusDays(1);

            // compareDate가 다음 파티션 시작일 이후면 생성 필요
            return !compareDate.isBefore(nextPartitionDate);
        }
        return false;
    }

    /* 다음 파티션 명 구하기 */
    public static String getNextPartitionName(PartitionRule partitionRule, String lastPartitionName) {
        if (isNeedCreateRangePartition ( partitionRule, lastPartitionName )) {
            RangePartition rangePartition = (RangePartition) partitionRule;
            LocalDate compareDate = getCompareDate(rangePartition.getPartitionRangeType());

            /* 분기,반기 포함 날짜형식 및 파티션 numbering 추출 */
            String lastPartitionNamePart = getPartitionPartName(lastPartitionName);
            /* 마지막파티션 년도 */
            Integer lastPartitionYyyy = Integer.parseInt(lastPartitionNamePart.substring(0, 4));
            /* 비교대상 년도 */
            Integer compareYear = compareDate.getYear();

            PartitionRangeType rangeType = rangePartition.getPartitionRangeType();

            String partitionName = partitionRule.getTableName() + "_p";

            return switch (rangeType) {
                case MONTHLY -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(rangeType.getLabelPattern());
                    LocalDate lastPartitionDate = getLocalDateByString(lastPartitionNamePart);
                    yield partitionName + formatter.format(lastPartitionDate.plus(rangeType.getPeriod(), rangeType.getUnit()));
                }
                case QUARTERLY -> {
                    /* 기수 (분기/반기) */
                    Integer lastQuarterIndex = Integer.parseInt(lastPartitionNamePart.substring(5, 6));
                    if (lastQuarterIndex == 4) {
                        yield partitionName + (lastPartitionYyyy + 1) + "Q1";
                    } else {
                        yield partitionName + lastPartitionYyyy + "Q" + (lastQuarterIndex + 1);
                    }
                }
                case HALF_YEARLY -> {
                    Integer lastHalfIndex = Integer.parseInt(lastPartitionNamePart.substring(5, 6));
                    if (lastHalfIndex == 2) {
                        yield partitionName + (lastPartitionYyyy + 1) + "H1";
                    } else {
                        yield partitionName + lastPartitionYyyy + "H" + (lastHalfIndex + 1);
                    }
                }
                case YEARLY -> partitionName + (lastPartitionYyyy + 1);
            };
        }
        return null;
    }

    /* 필요한 파티션명 모두 가져오기 */
    public static List<String> getAllNeededPartitionNames (PartitionRule partitionRule, String lastPartitionName) {
        List<String> partitionNames = new ArrayList<>();
        String nextPartitionName = getNextPartitionName(partitionRule, lastPartitionName);
        while (nextPartitionName != null) {
            partitionNames.add(nextPartitionName);
            nextPartitionName = getNextPartitionName(partitionRule, nextPartitionName);
        }
        return partitionNames;
    }

    /* 파티션 정보 Dto 생성 */
    public static PartitionRangeDto getPartitionRangeDto (PartitionRule partitionRule, String partitionName) {
        if (isRangePartition(partitionRule)) {
            RangePartition rangePartition = (RangePartition) partitionRule;
            PartitionRangeType rangeType = rangePartition.getPartitionRangeType();
            String partitionNamePart = getPartitionPartName(partitionName);

            LocalDate fromDate = switch (rangeType) {
                case MONTHLY -> getLocalDateByString(partitionNamePart);
                case QUARTERLY -> {
                    int year = Integer.parseInt(partitionNamePart.substring(0, 4));
                    int q = Integer.parseInt(partitionNamePart.substring(5, 6));
                    int month = (q - 1) * 3 + 1;
                    yield LocalDate.of(year, month, 1);
                }
                case HALF_YEARLY -> {
                    int year = Integer.parseInt(partitionNamePart.substring(0, 4));
                    int h = Integer.parseInt(partitionNamePart.substring(5, 6));
                    int month = (h - 1) * 6 + 1;
                    yield LocalDate.of(year, month, 1);
                }
                case YEARLY -> {
                    int year = Integer.parseInt(partitionNamePart.substring(0, 4));
                    yield LocalDate.of(year, 1, 1);
                }
            };

            LocalDate toDate = fromDate.plus(rangeType.getPeriod(), rangeType.getUnit());

            return PartitionRangeDto
                    .builder()
                    .fromDate(fromDate)
                    .toDate(toDate)
                    .partitionName(partitionName)
                    .build();
        }
        return null;
    }

    /* range 파티션인지 확인 */
    public static boolean isRangePartition ( PartitionRule partitionRule )  {
        if (partitionRule instanceof RangePartition) {
            return true;
        }
        return false;
    }

    /* rangePartitionInfoMap 가져오기 */
    public static Map<String, Object> getRangePartitionInfoMap (PartitionRule rule, String partitionTableName) {
        if (!isRangePartition(rule)) {
            return null;
        }
        Map<String, Object> rangePartitionInfo = new HashMap<>();

        PartitionRangeDto rangeDto = getPartitionRangeDto(rule, partitionTableName);

        RangePartition rangePartition = (RangePartition) rule;

        rangePartitionInfo.put("tableName", rule.getTableName());
        rangePartitionInfo.put("partitionTableName", partitionTableName);

        RangeFieldType RangeFieldType = rangePartition.getRangeFieldType();

        if (RangeFieldType == RangeFieldType.DATE) {
            rangePartitionInfo.put("fromDate", rangeDto.getFromDate());
            rangePartitionInfo.put("toDate", rangeDto.getToDate());
        } else if (RangeFieldType == RangeFieldType.TIMESTAMP) {
            rangePartitionInfo.put("fromDate", rangeDto.getFromDate().atStartOfDay());
            rangePartitionInfo.put("toDate", rangeDto.getToDate().atStartOfDay());
        }

        if (rule instanceof HashPartition hashPartition) {
            rangePartitionInfo.put("hashField", hashPartition.getHashField());
        }

        return rangePartitionInfo;
    }

    public static String getDetachPartitionName ( PartitionRule partitionRule ) {

        if (!isRangePartition(partitionRule)) {
            return null;
        }

        String targetPartitionTableName = partitionRule.getTableName() + "_p";

        ChronoUnit periodUnit = partitionRule.getDataStoredPeriodUnit();
        Integer period = partitionRule.getDataStoredPeriod();

        LocalDate standDate = LocalDate.now();
        standDate = standDate.minus(period, periodUnit).withDayOfMonth(1);

        switch (periodUnit) {
            case MONTHS -> standDate = standDate.withDayOfMonth(1);
            case YEARS -> standDate = standDate.withDayOfYear(1);
        }

        RangePartition rangePartition = (RangePartition) partitionRule;
        PartitionRangeType rangeType = rangePartition.getPartitionRangeType();

        standDate = standDate.minus(rangeType.getPeriod(), rangeType.getUnit());

        return switch (rangeType) {
            case MONTHLY -> targetPartitionTableName + standDate.format(DateTimeFormatter.ofPattern(rangeType.getLabelPattern()));
            case QUARTERLY -> targetPartitionTableName + standDate.getYear() + "Q" + (standDate.getMonthValue() / 3 + 1);
            case HALF_YEARLY -> targetPartitionTableName + standDate.getYear() + "H" + (standDate.getMonthValue() / 6 + 1);
            case YEARLY -> targetPartitionTableName + standDate.getYear();
        };
    }

    /* 파티션 체크에 필요한 이름 가져오기 */
    protected static String getPartitionPartName (String lastPartitionName) {
        /* 파티션 테이블 명을 '_' 기준으로 나눔 */
        String[] partitionNameParts = lastPartitionName.split("_");
        return partitionNameParts[partitionNameParts.length - 1].replaceAll(".*?((\\d+)([QHqh]\\d)?)$", "$1").toUpperCase();
    }

    /* 비교일자 가져오기 */
    protected static LocalDate getCompareDate (PartitionRangeType rangeType) {
        return LocalDate.now().plusDays(1).withDayOfMonth(1).plus(rangeType.getPeriod(), rangeType.getUnit());
    }

    /* 문자열로 LocalDate 만들기 */
    protected static LocalDate getLocalDateByString (String yyyyMM) {
        return LocalDate.parse(yyyyMM +"01", DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
