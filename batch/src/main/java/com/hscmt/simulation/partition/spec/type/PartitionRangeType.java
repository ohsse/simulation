package com.hscmt.simulation.partition.spec.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@RequiredArgsConstructor
public enum PartitionRangeType {
    MONTHLY(1, ChronoUnit.MONTHS, "yyyyMM"),
    QUARTERLY(3, ChronoUnit.MONTHS, "Q" ),
    HALF_YEARLY(6, ChronoUnit.MONTHS, "H" ),
    YEARLY(1, ChronoUnit.YEARS, "yyyy" ),
    ;
    private final Integer period;
    private final ChronoUnit unit;
    private final String labelPattern;

    public String getRangeName (LocalDateTime targetDateTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");

        return switch (this) {
            case MONTHLY -> {
                formatter =  DateTimeFormatter.ofPattern("yyyyMM");
                yield  formatter.format(targetDateTime);
            }
            case QUARTERLY -> targetDateTime.format(formatter) + "_Q" + getQuarter(targetDateTime);
            case HALF_YEARLY -> targetDateTime.format(formatter) + "_H" + getHalf(targetDateTime);
            case YEARLY -> targetDateTime.format(formatter);
        };
    }

    private Integer getQuarter (LocalDateTime targetDateTime) {
        return ((targetDateTime.getMonthValue() - 1)/3)+1;
    }

    private Integer getHalf (LocalDateTime targetDateTime) {
        return targetDateTime.getMonthValue() >= 6 ? 2 : 1;
    }
}
