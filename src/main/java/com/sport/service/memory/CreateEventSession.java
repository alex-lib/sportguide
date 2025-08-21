package com.sport.service.memory;
import com.sport.service.dto.EventDto;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CreateEventSession {
    private final Map<Long, EventDto> sessions = new ConcurrentHashMap<>();

    public EventDto getOrCreate(Long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new EventDto());
    }

    public void clear(Long chatId) {
        sessions.remove(chatId);
    }

    public boolean hasSession(Long chatId) {
        return sessions.containsKey(chatId);
    }

    public EventDto createSession(Long chatId) {
        EventDto dto = new EventDto();
        sessions.put(chatId, dto);
        return dto;
    }

    public EventDto getIfExists(Long chatId) {
        return sessions.get(chatId);
    }
}