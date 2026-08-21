package com.sport.service.store.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisCrudImpl implements RedisCrud {
    private final RedisTemplate<String, Object> botCommandSessionRedisTemplate;

    @Override
    public <T> T get(String key, Class<T> type) {
        Object value = botCommandSessionRedisTemplate.opsForValue().get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    @Override
    public void put(String key, Object value, long ttlSeconds) {
        botCommandSessionRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void delete(String key) {
        botCommandSessionRedisTemplate.delete(key);
    }
}