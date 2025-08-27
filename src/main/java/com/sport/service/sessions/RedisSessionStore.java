package com.sport.service.sessions;

public interface RedisSessionStore {
	<T> T get(String key, Class<T> type);
	void put(String key, Object value, long ttlSeconds);
	void delete(String key);
//	boolean exists(String key);
}