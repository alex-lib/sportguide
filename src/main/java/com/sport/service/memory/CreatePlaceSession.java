package com.sport.service.memory;
import com.sport.service.dto.PlaceDto;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CreatePlaceSession {

    private final Map<Long, PlaceDto> sessions = new ConcurrentHashMap<>();

    public PlaceDto getOrCreate(Long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new PlaceDto());
    }

    public void clear(Long chatId) {
        sessions.remove(chatId);
    }

    public boolean hasSession(Long chatId) {
        return sessions.containsKey(chatId);
    }

    public PlaceDto createSession(Long chatId) {
        PlaceDto dto = new PlaceDto();
        sessions.put(chatId, dto);
        return dto;
    }

    public PlaceDto getIfExists(Long chatId) {
        return sessions.get(chatId);
    }
}
