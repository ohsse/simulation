package com.hscmt.common.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * Caffeine CacheManager 설정
     *
     * @return CaffeineCacheManager
     */
    // @Primary
    @Bean(name = "caffeineCacheManager")
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CustomCaffeineCacheManager();
//        cacheManager.setAsyncCacheMode(true);
        return cacheManager;
    }

    /**
     * CustomCaffeineCacheManager는 {@link CaffeineCacheManager}를 확장한
     * 특수화된 구현체로, 서로 다른 캐시 이름에 따라 캐시 구성을 정의할 수 있도록 합니다.
     *
     * 이 클래스는 "cache1min", "cache10min", "cache1hour", "cache1day"와 같이
     * 특정 캐시 이름에 대한 사용자 정의 캐시 사양을 정의합니다.
     * 이러한 각 구성은 캐시 만료 시간과 최대 크기를 지정합니다.
     * 만약 캐시 이름이 사전에 정의된 구성과 일치하지 않을 경우, 기본 캐시 사양이 사용됩니다.
     */
    static class CustomCaffeineCacheManager extends CaffeineCacheManager {

        private final Map<String, Caffeine<Object, Object>> cacheSpecs = new HashMap<>();

        private boolean asyncCache = false;

        private final Caffeine<Object, Object> defaultSpec = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000);

        public CustomCaffeineCacheManager() {
            // 캐시 이름별 설정
            cacheSpecs.put("cache1sec", Caffeine.newBuilder()
                    .expireAfterWrite(1, TimeUnit.SECONDS)
                    .maximumSize(1000));
            cacheSpecs.put("cache3sec", Caffeine.newBuilder()
                    .expireAfterWrite(3, TimeUnit.SECONDS)
                    .maximumSize(1000));
            cacheSpecs.put("cache5sec", Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS)
                    .maximumSize(1000));
            cacheSpecs.put("cache1min", Caffeine.newBuilder()
                    .expireAfterWrite(1, TimeUnit.MINUTES)
                    .maximumSize(1000));
            cacheSpecs.put("cache10min", Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .maximumSize(1000));
            cacheSpecs.put("cache1hour", Caffeine.newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .maximumSize(1000));
            cacheSpecs.put("cache1day", Caffeine.newBuilder()
                    .expireAfterWrite(1, TimeUnit.DAYS)
                    .maximumSize(1000));
        }

        @Override
        public void setAsyncCacheMode(boolean asyncCache) {
            super.setAsyncCacheMode(asyncCache);
            this.asyncCache = asyncCache; // 내장 메서드 외에 커스텀 내부 변수로도 저장
        }

        @Override
        protected CaffeineCache createCaffeineCache(String name) {
            Caffeine<Object, Object> config = cacheSpecs.getOrDefault(name, defaultSpec);
            if (asyncCache) {
                return new CaffeineCache(name, config.buildAsync(), isAllowNullValues());
            } else {
                return new CaffeineCache(name, config.build(), isAllowNullValues());
            }
        }
    }
}
