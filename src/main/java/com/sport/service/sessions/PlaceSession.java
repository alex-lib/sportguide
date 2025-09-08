package com.sport.service.sessions;
import com.sport.service.dto.PlaceDto;
import org.springframework.stereotype.Component;

@Component
public class PlaceSession {

	private static final String KEY_PREFIX = "session:place:";

    private static final long TTL_SECONDS = 300;

	private final RedisSessionStore store;

	public PlaceSession(RedisSessionStore store) {
		this.store = store;
	}

	private String key(Long chatId) {
		return KEY_PREFIX + chatId;
	}

	public void clear(Long chatId) {
		store.delete(key(chatId));
	}


	public PlaceDto createSession(Long chatId) {
		PlaceDto dto = new PlaceDto();
		store.put(key(chatId), dto, TTL_SECONDS);
		return dto;
	}

	public PlaceDto getIfExists(Long chatId) {
		return store.get(key(chatId), PlaceDto.class);
	}

	public void save(Long chatId, PlaceDto dto) {
		store.put(key(chatId), dto, TTL_SECONDS);
	}
}