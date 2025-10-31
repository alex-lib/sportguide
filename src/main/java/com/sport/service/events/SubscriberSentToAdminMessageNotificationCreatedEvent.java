package com.sport.service.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubscriberSentToAdminMessageNotificationCreatedEvent {
    private final String notification;
}