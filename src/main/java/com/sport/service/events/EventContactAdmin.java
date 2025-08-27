package com.sport.service.events;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.User;

@Getter
@RequiredArgsConstructor
public class EventContactAdmin {

    private final String text;
    private final User user;
}