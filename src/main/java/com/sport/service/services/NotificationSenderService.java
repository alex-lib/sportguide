package com.sport.service.services;

import java.util.List;

public interface NotificationSenderService {
    void sendWeatherNotification(String message, List<Long> userIds);

    void sendEventNotification(String message, List<Long> userIds);

    void sendSubscriberToAdminNotification(String message, Long userId);

    void sendAdminToSubscriberNotification(String message, byte[] photo, List<Long> userIds);
}