package com.sport.service.services.impl;

import com.sport.service.redis_store.notifications_broker.Notification;
import com.sport.service.redis_store.notifications_broker.NotificationPublisher;
import com.sport.service.services.NotificationSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationSenderServiceImpl implements NotificationSenderService {
    private final NotificationPublisher publisher;

    @Override
    public void sendWeatherNotification(String message, List<Long> userIds) {
        Notification template = Notification.builder()
                .type(Notification.NotificationType.WEATHER)
                .message(message)
                .build();
        publisher.publishToUsers(userIds, template);
    }

    @Override
    public void sendEventNotification(String message, List<Long> userIds) {
        Notification template = Notification.builder()
                .type(Notification.NotificationType.EVENT)
                .message(message)
                .build();
        publisher.publishToUsers(userIds, template);
    }

    @Override
    public void sendSubscriberToAdminNotification(String message, Long userId) {
        Notification template = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.SUBSCRIBER_TO_ADMIN)
                .message(message)
                .build();
        publisher.publish(template);
    }

    @Override
    public void sendAdminToSubscriberNotification(String message, byte[] photo, List<Long> userIds) {
        Notification template = Notification.builder()
                .type(Notification.NotificationType.EVENT)
                .message(message)
                .photo(photo)
                .build();
        publisher.publishToUsers(userIds, template);
    }
}