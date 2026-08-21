package com.sport.service.store.commands.sessions;

import com.sport.service.entities.enums.place.PlaceState;
import com.sport.service.store.commands.RedisCrud;
import org.springframework.stereotype.Component;

@Component
public class PlaceSession {
    private static final String KEY_PREFIX = "session:place:";
    private static final long TTL_SECONDS = 300;

    private final RedisCrud store;

    public PlaceSession(RedisCrud store) {
        this.store = store;
    }

    private String key(Long chatId) {
        return KEY_PREFIX + chatId;
    }

    public void clear(Long chatId) {
        store.delete(key(chatId));
    }

    public PlaceState createSession(Long chatId) {
        PlaceState state = new PlaceState();
        store.put(key(chatId), state, TTL_SECONDS);
        return state;
    }

    public PlaceState getIfExists(Long chatId) {
        return store.get(key(chatId), PlaceState.class);
    }

    public void save(Long chatId, PlaceState state) {
        store.put(key(chatId), state, TTL_SECONDS);
    }
}
