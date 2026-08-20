package com.sport.service.store.commands;

public interface RedisCrud {
    <T> T get(String key, Class<T> type);

    void put(String key, Object value, long ttlSeconds);

    void delete(String key);
}