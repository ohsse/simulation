package com.hscmt.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DateTimeUtil {

    public static List<LocalDateTime> getDateTimeList(LocalDateTime startDateTime, LocalDateTime endDateTime, ChronoUnit intervalUnit) {
        List<LocalDateTime> dateTimeList = new ArrayList<>();

        LocalDateTime start = startDateTime.truncatedTo(intervalUnit);
        LocalDateTime end = endDateTime.truncatedTo(intervalUnit);

        for (LocalDateTime t = start; !t.isAfter(end); t = t.plus(1, intervalUnit)){
            dateTimeList.add(t);
        }

        return dateTimeList;
    }

    public static LocalDateTime convertUuidStringToLocalDateTime(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return LocalDateTime.now();
        }
        UUID uuid = UUID.fromString(uuidString);
        if (uuid.version() != 7) {
            return LocalDateTime.now();
        }
        long msb = uuid.getMostSignificantBits();
        long epochMillis = (msb >>> 16) & 0x0000FFFFFFFFFFFFL; // 상위 48비트 = epoch(ms)
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
