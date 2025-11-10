package com.sport.service.redis_store.notifications_broker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSendingRecovery {
    @Qualifier("notificationRedisTemplate")
    private final RedisTemplate<String, Object> notificationRedisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void continueToSendInterruptedNotifications() {
        Set<String> keys = notificationRedisTemplate.keys("notification:*:*:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        log.info("There are {} notifications to resend", keys.size());
        for (String key : keys) {
            String[] parts = key.split(":");
            String type = parts[1];
            Long userId = Long.parseLong(parts[2]);
            String topic = "notification:" + type;
            notificationRedisTemplate.convertAndSend(topic, userId.toString());
        }
    }
}