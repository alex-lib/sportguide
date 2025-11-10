package com.sport.service.redis_store.commands_store;

public interface RedisCrud {
    <T> T get(String key, Class<T> type);

    void put(String key, Object value, long ttlSeconds);

    void delete(String key);
}