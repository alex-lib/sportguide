package com.sport.service.services;

import com.sport.service.bot.TelegramMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisHealthAlertService {
    private final RedisConnectionFactory redisConnectionFactory;
    private final TelegramMessageSender telegramMessageSender;
    private final NotificationCreatorService notificationCreatorService;

    @Value("${telegram.mainAdminId}")
    private String adminId;

    private final AtomicBoolean redisDown = new AtomicBoolean(false);
    private final AtomicBoolean alertSent = new AtomicBoolean(false);
    private final AtomicLong lastAlertTime = new AtomicLong(0);
    private static final long ALERT_COOLDOWN_MINUTES = 10;

    @Scheduled(cron = "*/30 * * * * *")
    public void checkRedisHealth() {
        try {
            boolean connected = redisConnectionFactory.getConnection().ping() != null;
            if (!connected) {
                handleRedisDown();
            } else {
                handleRedisUp();
            }
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            handleRedisDown();
        }
    }

    private void handleRedisDown() {
        if (redisDown.compareAndSet(false, true)) {
            log.warn("Redis is DOWN! Sending alert to admin.");
            String message = notificationCreatorService.createRedisDownAlert(java.time.LocalDateTime.now());
            telegramMessageSender.sendMessageWithoutPhoto(Long.valueOf(adminId), message);
            alertSent.set(true);
            lastAlertTime.set(System.currentTimeMillis());
        } else if (alertSent.get()) {
            long now = System.currentTimeMillis();
            long lastAlert = lastAlertTime.get();
            long diffMinutes = (now - lastAlert) / (1000 * 60);
            if (diffMinutes >= ALERT_COOLDOWN_MINUTES) {
                log.warn("Redis is still down. Sending reminder alert to admin.");
                String message = notificationCreatorService.createRedisDownReminderAlert(diffMinutes);
                telegramMessageSender.sendMessageWithoutPhoto(Long.valueOf(adminId), message);
                alertSent.set(true);
                lastAlertTime.set(System.currentTimeMillis());
            }
        }
    }

    private void handleRedisUp() {
        if (redisDown.compareAndSet(true, false)) {
            log.info("Redis is back UP! Sending recovery alert to admin.");
            String message = notificationCreatorService.createRedisUpAlert(java.time.LocalDateTime.now());
            telegramMessageSender.sendMessageWithoutPhoto(Long.valueOf(adminId), message);
            alertSent.set(false);
            lastAlertTime.set(0);
        }
    }
}
