package com.sport.service.components;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaceCacheEvictScheduler {
    private final CacheManager cacheManager;

    @Scheduled(cron = "0 0 */10 * * *")
    public void evictPlacesCache() {
        Cache cache = cacheManager.getCache("places");
        if (cache != null) {
            cache.clear();
        }
    }
}
