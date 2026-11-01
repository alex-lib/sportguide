package com.sport.service.services.eventimport;

import com.sport.service.services.NotificationPreferenceService;
import com.sport.service.store.notifications.Notification;
import com.sport.service.store.notifications.NotificationPublisher;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventImportMessageDelivery {
    private final JdbcTemplate jdbc;
    private final NotificationPublisher publisher;
    private final NotificationPreferenceService preferences;

    @Transactional
    public void deliver(UUID id) {
        var pending =
                jdbc.query(
                        "SELECT user_id, message FROM event_import_messages "
                                + "WHERE id = ? AND queued_at IS NULL FOR UPDATE SKIP LOCKED",
                        (rs, row) ->
                                Notification.builder()
                                        .id(id)
                                        .userId(rs.getLong("user_id"))
                                        .type(Notification.NotificationType.EVENT)
                                        .message(rs.getString("message"))
                                        .build(),
                        id);
        if (pending.isEmpty()) return;
        var notification = pending.getFirst();
        if (preferences.isEnabled(notification.getUserId(), Notification.NotificationType.EVENT)) {
            publisher.publish(notification);
        }
        jdbc.update(
                "UPDATE event_import_messages SET queued_at = CURRENT_TIMESTAMP WHERE id = ?", id);
    }
}
