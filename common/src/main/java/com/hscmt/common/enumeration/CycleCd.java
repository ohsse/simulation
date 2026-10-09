package com.hscmt.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Getter
public enum CycleCd {
    MIN(ChronoUnit.MINUTES),
    HOUR(ChronoUnit.HOURS),
    DAY(ChronoUnit.DAYS),
    MON(ChronoUnit.MONTHS),
    YEAR(ChronoUnit.YEARS),
    ;
    private final ChronoUnit unit;
}
