package com.sport.service.services.eventimport;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sport.service.entities.Event;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.services.NotificationPreferenceService;
import com.sport.service.store.notifications.Notification;
import com.sport.service.store.notifications.NotificationPublisher;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.UUID;

class EventImportMessagesTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final NotificationPreferenceService preferences =
            mock(NotificationPreferenceService.class);
    private final NotificationCreatorService creator = mock(NotificationCreatorService.class);
    private final EventImportMessageDelivery delivery = mock(EventImportMessageDelivery.class);
    private final EventImportMessages messages =
            new EventImportMessages(jdbc, creator, preferences, delivery);

    @Test
    void persistsSeparateMessagesOnlyForSubscribersBeforeAnySending() {
        Event event = Event.builder().id(3L).build();
        when(creator.createEventNotification(event)).thenReturn("Матч");
        when(preferences.recipients(Notification.NotificationType.EVENT))
                .thenReturn(List.of(10L, 20L));
        messages.queue(event, "Через неделю: ");
        verify(jdbc)
                .update(
                        startsWith("INSERT INTO event_import_messages"),
                        any(UUID.class),
                        eq(3L),
                        eq(10L),
                        eq("Через неделю: Матч"));
        verify(jdbc)
                .update(
                        startsWith("INSERT INTO event_import_messages"),
                        any(UUID.class),
                        eq(3L),
                        eq(20L),
                        eq("Через неделю: Матч"));
        verifyNoInteractions(delivery);
    }

    @Test
    @SuppressWarnings("unchecked")
    void failedRecipientDoesNotPreventOtherPendingMessagesFromBeingQueued() {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of(first, second));
        doThrow(new RuntimeException("Redis unavailable")).when(delivery).deliver(first);
        messages.sendPending();
        verify(delivery).deliver(first);
        verify(delivery).deliver(second);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deliveryRechecksSubscriptionAndDoesNotMarkFailedRedisWriteAsQueued() {
        var publisher = mock(NotificationPublisher.class);
        var worker = new EventImportMessageDelivery(jdbc, publisher, preferences);
        UUID id = UUID.randomUUID();
        var notification =
                Notification.builder()
                        .id(id)
                        .userId(10L)
                        .type(Notification.NotificationType.EVENT)
                        .message("Матч")
                        .build();
        when(jdbc.query(anyString(), any(RowMapper.class), eq(id)))
                .thenReturn(List.of(notification));
        when(preferences.isEnabled(10L, Notification.NotificationType.EVENT)).thenReturn(true);
        doThrow(new RuntimeException("Redis unavailable")).when(publisher).publish(notification);
        assertThatThrownBy(() -> worker.deliver(id)).hasMessage("Redis unavailable");
        verify(jdbc, never()).update(anyString(), eq(id));
        when(preferences.isEnabled(10L, Notification.NotificationType.EVENT)).thenReturn(false);
        worker.deliver(id);
        verify(publisher).publish(notification);
        verify(jdbc).update(startsWith("UPDATE event_import_messages"), eq(id));
    }
}
