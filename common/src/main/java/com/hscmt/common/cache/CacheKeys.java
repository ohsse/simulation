package com.hscmt.common.cache;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class CacheKeys {
    private CacheKeys() {}

    public static final String SEPARATOR = "|";

    /** LocalDateTime을 분 단위로 잘라 문자열(ISO-8601)로 반환 */
    public static String minute(LocalDateTime t) {
        return t == null ? "null"
                : t.truncatedTo(ChronoUnit.MINUTES).toString();
    }

    /** 파츠를 규칙(stringify)대로 문자열로 바꿔 '|'로 조인 */
    public static String join(Object... parts) {
        return Stream.of(parts)
                .map(CacheKeys::stringify)
                .collect(Collectors.joining(SEPARATOR));
    }

    /** prefix를 맨 앞에 붙여 키 생성 */
    public static String generateKey(String prefix, Object... parts) {
        return Stream.concat(Stream.of(prefix+SEPARATOR), Stream.of(parts).map(CacheKeys::stringify))
                .collect(Collectors.joining(SEPARATOR));
    }

    /** 규칙: LocalDateTime → minute(), 그 외 → toString(); null → "null" */
    private static String stringify(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof LocalDateTime) {
            return minute((LocalDateTime) obj);
        }

        // 배열이면 내부 요소도 같은 규칙으로 평탄화해서 쉼표로 연결
        if (obj.getClass().isArray()) {
            // Object[]가 아닐 수도 있어 primitive 배열 가능성 대비
            if (obj instanceof Object[]) {
                return Arrays.stream((Object[]) obj)
                        .map(CacheKeys::stringify)
                        .collect(Collectors.joining(","));
            } else {
                // primitive 배열은 Arrays.toString으로 처리
                return primitiveArrayToString(obj);
            }
        }

        // Iterable이면 요소들을 같은 규칙으로 처리
        if (obj instanceof Iterable<?>) {
            return StreamSupportUtils.stream((Iterable<?>) obj)
                    .map(CacheKeys::stringify)
                    .collect(Collectors.joining(","));
        }

        return obj.toString();
    }

    // primitive 배열 대비 유틸
    private static String primitiveArrayToString(Object array) {
        if (array instanceof int[])    return Arrays.toString((int[]) array);
        if (array instanceof long[])   return Arrays.toString((long[]) array);
        if (array instanceof double[]) return Arrays.toString((double[]) array);
        if (array instanceof float[])  return Arrays.toString((float[]) array);
        if (array instanceof short[])  return Arrays.toString((short[]) array);
        if (array instanceof byte[])   return Arrays.toString((byte[]) array);
        if (array instanceof char[])   return Arrays.toString((char[]) array);
        if (array instanceof boolean[])return Arrays.toString((boolean[]) array);
        return array.toString();
    }

    /** Iterable → Stream (의존성 없이 간단 유틸) */
    private static final class StreamSupportUtils {
        static <T> Stream<T> stream(Iterable<T> it) {
            return it == null ? Stream.empty() : StreamSupport.stream(it.spliterator(), false);
        }
    }
}
