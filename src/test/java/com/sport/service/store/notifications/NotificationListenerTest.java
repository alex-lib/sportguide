package com.sport.service.store.notifications;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.entities.Subscriber;
import com.sport.service.repositories.SubscriberRepository;
import com.sport.service.services.NotificationPreferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationListenerTest {
    @SuppressWarnings("unchecked") private final RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked") private final ValueOperations<String, Object> values = mock(ValueOperations.class);
    private final TelegramMessageSender sender = mock(TelegramMessageSender.class);
    private final SubscriberRepository subscribers = mock(SubscriberRepository.class);
    private final NotificationListener listener = new NotificationListener(redis, sender, new NotificationPreferenceService(subscribers));

    @ParameterizedTest
    @EnumSource(value = Notification.NotificationType.class, names = {"WEATHER", "EVENT", "PLACE", "COACH", "JOINT_TRAINING"})
    void optingOutAlsoStopsAlreadyQueuedNotifications(Notification.NotificationType type) {
        Subscriber subscriber = Subscriber.builder().getEvents(false).getWeather(false)
                .getPlaces(false).getCoaches(false).build();
        when(subscribers.findById(10L)).thenReturn(Optional.of(subscriber));
        deliver(type, null);
        verifyNoInteractions(sender);
        verify(redis).delete("queued");
    }

    @Test
    void adminTextAndPhotoArriveRegardlessOfPreferences() {
        deliver(Notification.NotificationType.ADMIN_TO_SUBSCRIBER, null);
        verify(sender).sendMessageWithoutPhoto(10L, "Сообщение");
        byte[] photo = {1};
        deliver(Notification.NotificationType.ADMIN_TO_SUBSCRIBER, photo);
        verify(sender).sendMessageWithPhoto(10L, photo, "Сообщение");
        verifyNoInteractions(subscribers);
    }

    private void deliver(Notification.NotificationType type, byte[] photo) {
        when(redis.opsForValue()).thenReturn(values);
        when(redis.keys(anyString())).thenReturn(Set.of("queued"));
        when(values.get("queued")).thenReturn(Notification.builder().userId(10L).type(type).message("Сообщение").photo(photo).build());
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn("10".getBytes(StandardCharsets.UTF_8));
        listener.onMessage(message, "notification:test".getBytes(StandardCharsets.UTF_8));
    }
}
