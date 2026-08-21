package com.sport.service.store.notifications;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification implements Serializable {
    private UUID id;
    private Long userId;
    private NotificationType type;
    private String message;
    private byte[] photo;

    public enum NotificationType {
        EVENT,
        WEATHER,
        ADMIN_TO_SUBSCRIBER,
        SUBSCRIBER_TO_ADMIN,
        ADMIN_ALERT
    }
}