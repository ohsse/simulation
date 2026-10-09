package com.hscmt.common.util;


import java.io.Serial;
import java.util.LinkedHashMap;

@SuppressWarnings("unchecked")
public class HscmtMap<K, V> extends LinkedHashMap<K, V> {

    @Serial
    private static final long serialVersionUID = 8489854690075749676L;

    @Override
    public V put(K key, V value) {
        String keyStr = key.toString();
        return super.put((K) StringUtil.convertToCamelCase(keyStr), value);
    }

    /**
     * put
     *
     * @param key     키
     * @param value   값
     * @param isCamel 카멜케이스표현 여부 true>camel/false>원본
     */
    public V put(K key, V value, boolean isCamel) {
        if (isCamel) return put(key, value);
        else return super.put(key, value);
    }
}