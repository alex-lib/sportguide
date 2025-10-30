package com.sport.service.events;

import com.sport.service.entities.subscriber.Subscriber;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class EventNotificationCreatedEvent {
    private final List<Subscriber> subscribers;
    private final String notification;
}