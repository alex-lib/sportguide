package com.sport.service.sessions;

import com.sport.service.dto.MessageDto;
import org.springframework.stereotype.Component;

@Component
public class MessageSession {

    private static final String KEY_PREFIX = "session:message:";

    private static final long TTL_SECONDS = 300;

    private final RedisSessionStore store;

    public MessageSession(RedisSessionStore store) {
        this.store = store;
    }

    private String key(Long chatId) {
        return KEY_PREFIX + chatId;
    }

    public void clear(Long chatId) {
        store.delete(key(chatId));
    }

    public MessageDto createSession(Long chatId) {
        MessageDto dto = new MessageDto();
        store.put(key(chatId), dto, TTL_SECONDS);
        return dto;
    }

    public MessageDto getIfExists(Long chatId) {
        return store.get(key(chatId), MessageDto.class);
    }

    public void save(Long chatId, MessageDto dto) {
        store.put(key(chatId), dto, TTL_SECONDS);
    }
}
