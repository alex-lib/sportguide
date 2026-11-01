package com.sport.service.services.eventimport;

import com.sport.service.entities.Event;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.services.NotificationPreferenceService;
import com.sport.service.store.notifications.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventImportMessages {
    private final JdbcTemplate jdbc;
    private final NotificationCreatorService creator;
    private final NotificationPreferenceService preferences;
    private final EventImportMessageDelivery delivery;

    @Transactional
    public void queue(Event event, String prefix) {
        String message = prefix + creator.createEventNotification(event);
        for (Long userId : preferences.recipients(Notification.NotificationType.EVENT)) {
            jdbc.update(
                    "INSERT INTO event_import_messages(id, event_id, user_id, message) VALUES (?,"
                        + " ?, ?, ?)",
                    UUID.randomUUID(),
                    event.getId(),
                    userId,
                    message);
        }
    }

    @Transactional
    public void replacePending(Event event, String prefix) {
        jdbc.update(
                "DELETE FROM event_import_messages WHERE event_id = ? AND queued_at IS NULL",
                event.getId());
        queue(event, prefix);
    }

    public void sendPending() {
        var ids =
                jdbc.query(
                        "SELECT id FROM event_import_messages WHERE queued_at IS NULL ORDER BY"
                            + " created_at LIMIT 500",
                        (rs, row) -> rs.getObject("id", UUID.class));
        for (UUID id : ids) {
            try {
                delivery.deliver(id);
            } catch (RuntimeException e) {
                log.error("Cannot queue event notification {}; will retry", id, e);
            }
        }
    }
}
