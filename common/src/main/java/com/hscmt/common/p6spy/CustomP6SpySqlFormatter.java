package com.hscmt.common.p6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/* P6Spy 로그는 기본적으로 Sql Pretty Format 지원 안해서 별도로 적용 */
public class CustomP6SpySqlFormatter implements MessageFormattingStrategy {
    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        long timestamp = Long.parseLong(now);
        LocalDateTime nowDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String returnString = "";
        if (url != null && url.contains("jdbc:p6spy:")) {
            returnString = returnString + this.formatSql(sql);
        } else {
            returnString = formatter.format(nowDateTime) + " | " + elapsed + "ms | " + category + " | connection " + connectionId + " | url " + url;
        }

        return returnString;
    }

    protected String formatSql(String sql) {
        return (new BasicFormatterImpl()).format(sql);
    }
}
