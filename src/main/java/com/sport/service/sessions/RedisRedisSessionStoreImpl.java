package com.sport.service.sessions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class RedisRedisSessionStoreImpl implements RedisSessionStore {

	private final RedisTemplate<String, Object> redis;

	public RedisRedisSessionStoreImpl(RedisTemplate<String, Object> redis) {
		this.redis = redis;
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		Object value = redis.opsForValue().get(key);
		return type.isInstance(value) ? type.cast(value) : null;
	}

	@Override
	public void put(String key, Object value, long ttlSeconds) {
		redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
	}

	@Override
	public void delete(String key) {
		redis.delete(key);
	}
}