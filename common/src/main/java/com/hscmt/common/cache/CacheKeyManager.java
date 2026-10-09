package com.hscmt.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheKeyManager {
    @Qualifier("caffeineCacheManager")
    private final CaffeineCacheManager caffeineCacheManager;

    public final String SEP = "|";

    protected String prefixOf(String prefix) {
        return prefix.endsWith(SEP) ? prefix : prefix + SEP;
    }

    /** 특정 캐시에서 prefix(= 'groupTree' 같은 논리 접두어)로 시작하는 키만 제거하고 제거 건수를 반환 */
    public int evictByPrefix(String cacheName, String logicalPrefix) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache == null) return 0;

        String actualPrefix = prefixOf(logicalPrefix); // "prefix|"
        int removed = 0;

        if (cache instanceof CaffeineCache cc) {
            Object nativeCache = cc.getNativeCache();
            ConcurrentMap<?, ?> map;

            if (nativeCache instanceof com.github.benmanes.caffeine.cache.AsyncCache<?, ?> async) {
                map = async.synchronous().asMap();
            } else if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> sync) {
                map = sync.asMap();
            } else {
                cache.clear();
                log.debug("Cleared entire cache '{}' due to unknown native cache type", cacheName);
                return -1; // 전체 초기화 표시
            }

            // 안전한 remove 루프
            for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) it.next();
                Object k = e.getKey();
                if (k instanceof String s && s.startsWith(actualPrefix)) {
                    it.remove();
                    removed++;
                }
            }
            if (removed > 0) {
                log.debug("Evicted {} entries from cache '{}' by prefix '{}'", removed, cacheName, actualPrefix);
            }
            return removed;
        } else {
            cache.clear();
            log.debug("Cleared entire cache '{}' (non-Caffeine Cache)", cacheName);
            return -1;
        }
    }

    /** 모든 Caffeine 캐시에 대해 논리 prefix로 제거 (스코프가 넓으니 주의) */
    public int evictByPrefixAllCaches(String logicalPrefix) {
        int total = 0;
        for (String name : caffeineCacheManager.getCacheNames()) {
            int c = evictByPrefix(name, logicalPrefix);
            total += Math.max(c, 0); // -1(전체초기화)는 카운트 제외
        }
        return total;
    }

    /** 정확히 일치하는 풀키를 제거 (Spring Cache 추상화 활용) */
    public void evictExact(String cacheName, String fullKey) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evictIfPresent(fullKey);
            log.debug("Evicted exact key '{}' from cache '{}'", fullKey, cacheName);
        }
    }
}
