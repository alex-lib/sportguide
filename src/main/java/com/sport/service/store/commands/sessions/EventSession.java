package com.sport.service.store.commands.sessions;

import com.sport.service.entities.enums.event.EventState;
import com.sport.service.store.commands.RedisCrud;
import org.springframework.stereotype.Component;

@Component
public class EventSession {
    private static final String KEY_PREFIX = "session:event:";
    private static final long TTL_SECONDS = 300;

    private final RedisCrud store;

    public EventSession(RedisCrud store) {
        this.store = store;
    }

    private String key(Long chatId) {
        return KEY_PREFIX + chatId;
    }

    public void clear(Long chatId) {
        store.delete(key(chatId));
    }

    public EventState createSession(Long chatId) {
        EventState state = new EventState();
        state.setStep(CreateEventStep.DISTRICT);
        store.put(key(chatId), state, TTL_SECONDS);
        return state;
    }

    public EventState getIfExists(Long chatId) {
        return store.get(key(chatId), EventState.class);
    }

    public void save(Long chatId, EventState state) {
        store.put(key(chatId), state, TTL_SECONDS);
    }
}
