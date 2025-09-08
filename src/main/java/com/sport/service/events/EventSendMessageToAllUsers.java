package com.sport.service.events;

import com.sport.service.entities.subscriber.Subscriber;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class EventSendMessageToAllUsers {

    private final String text;

    private final List<Subscriber> subscribers;
}