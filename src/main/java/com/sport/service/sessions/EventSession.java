package com.sport.service.sessions;
import com.sport.service.dto.EventDto;
import org.springframework.stereotype.Component;

@Component
public class EventSession {

	private static final String KEY_PREFIX = "session:event:";

    private static final long TTL_SECONDS = 300;

	private final RedisSessionStore store;

	public EventSession(RedisSessionStore store) {
		this.store = store;
	}

	private String key(Long chatId) {
		return KEY_PREFIX + chatId;
	}

	public void clear(Long chatId) {
		store.delete(key(chatId));
	}

	public EventDto createSession(Long chatId) {
		EventDto dto = new EventDto();
		store.put(key(chatId), dto, TTL_SECONDS);
		return dto;
	}

	public EventDto getIfExists(Long chatId) {
		return store.get(key(chatId), EventDto.class);
	}

	public void save(Long chatId, EventDto dto) {
		store.put(key(chatId), dto, TTL_SECONDS);
	}
}