package com.sport.service.redis_store.notifications_broker;

import com.sport.service.bot.TelegramMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationListener implements MessageListener {
    @Qualifier("notificationRedisTemplate")
    private final RedisTemplate<String, Object> notificationRedisTemplate;

    private final TelegramMessageSender telegramMessageSender;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        log.info("Received message on channel: {}", channel);
        String userIdString = new String(message.getBody());
        Long userId = Long.parseLong(userIdString);

        String keyPrefix = String.format("%s:%d:*", channel, userId);
        Set<String> keys = notificationRedisTemplate.keys(keyPrefix);

        for (String key : keys) {
            Notification notification = (Notification) notificationRedisTemplate.opsForValue().get(key);
            sendNotification(notification);
            notificationRedisTemplate.delete(key);
        }
    }

    //    @Scheduled(fixedRate = 35)
    private void sendNotification(Notification notification) {
        Long userId = notification.getUserId();
        String message = notification.getMessage();
        byte[] photo = notification.getPhoto();

        if (photo != null) {
            telegramMessageSender.sendMessageWithPhoto(userId, photo, message);
        } else {
            telegramMessageSender.sendMessageWithoutPhoto(userId, message);
        }
    }
}