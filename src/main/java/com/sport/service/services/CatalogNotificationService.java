package com.sport.service.services;

import com.sport.service.store.notifications.Notification;
import com.sport.service.store.notifications.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogNotificationService {
    private final NotificationPreferenceService preferences;
    private final NotificationPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(Notification notification) {
        try {
            publisher.publishToUsers(preferences.recipients(notification.getType()), notification);
        } catch (RuntimeException e) {
            log.error("Failed to queue {} notification after commit", notification.getType(), e);
        }
    }
}
