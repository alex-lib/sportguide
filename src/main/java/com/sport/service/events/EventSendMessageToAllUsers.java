package com.sport.service.events;

import com.sport.service.entities.subscriber.Subscriber;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class EventSendMessageToAllUsers {
    private final String message;
    private final byte[] photo;
    private final List<Subscriber> subscribers;
}