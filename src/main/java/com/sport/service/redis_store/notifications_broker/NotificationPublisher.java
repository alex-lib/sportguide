package com.sport.service.redis_store.notifications_broker;

import com.sport.service.constants.RestConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
    @Qualifier("notificationRedisTemplate")
    private final RedisTemplate<String, Object> notificationRedisTemplate;

    public void publish(Notification notification) {
        String topic = getTopic(notification.getType());
        String key = buildKey(notification);
        notificationRedisTemplate.opsForValue().set(key, notification);
        notificationRedisTemplate.expire(key, Duration.ofHours(24));
        notificationRedisTemplate.convertAndSend(topic, notification.getUserId());
    }

    public void publishToUsers(List<Long> userIds, Notification template) {
        for (Long userId : userIds) {
            Notification userNotification = Notification.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .type(template.getType())
                    .message(template.getMessage())
                    .photo(template.getPhoto())
                    .build();
            publish(userNotification);
        }
    }

    private String getTopic(Notification.NotificationType type) {
        return switch (type) {
            case EVENT -> RestConstants.EVENT_CHANNEL_NAME;
            case WEATHER -> RestConstants.WEATHER_CHANNEL_NAME;
            case ADMIN_TO_SUBSCRIBER -> RestConstants.ADMIN_TO_SUBSCRIBER_CHANNEL_NAME;
            case SUBSCRIBER_TO_ADMIN -> RestConstants.SUBSCRIBER_TO_ADMIN_CHANNEL_NAME;
        };
    }

    private String buildKey(Notification notification) {
        return String.format(
                RestConstants.KEY_OF_CHANNEL_NAME + "%s:%d:%s",
                notification.getType().name().toLowerCase(),
                notification.getUserId(),
                notification.getId()
        );
    }
}