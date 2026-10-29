package com.sport.service.services;

import com.sport.service.store.notifications.Notification;
import com.sport.service.store.notifications.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(CatalogNotificationServiceTest.Config.class)
class CatalogNotificationServiceTest {
    @Autowired private ApplicationEventPublisher events;
    @Autowired private NotificationPublisher publisher;
    @Autowired private NotificationPreferenceService preferences;
    @Autowired private TransactionTemplate transaction;

    @BeforeEach
    void setUp() { reset(publisher, preferences); }

    @Test
    void notificationIsQueuedOnlyAfterCommit() {
        Notification notification = Notification.builder().type(Notification.NotificationType.PLACE).message("Новое место").build();
        when(preferences.recipients(Notification.NotificationType.PLACE)).thenReturn(List.of(10L));
        transaction.executeWithoutResult(status -> {
            events.publishEvent(notification);
            verifyNoInteractions(publisher);
        });
        verify(publisher).publishToUsers(List.of(10L), notification);
    }

    @Test
    void failedCreationDoesNotAnnounceUnsavedCoach() {
        transaction.executeWithoutResult(status -> {
            events.publishEvent(Notification.builder().type(Notification.NotificationType.COACH).build());
            status.setRollbackOnly();
        });
        verifyNoInteractions(publisher, preferences);
    }

    @Configuration
    @EnableTransactionManagement
    @Import(CatalogNotificationService.class)
    static class Config {
        @Bean NotificationPublisher publisher() { return mock(NotificationPublisher.class); }
        @Bean NotificationPreferenceService preferences() { return mock(NotificationPreferenceService.class); }
        @Bean TransactionTemplate transaction() { return new TransactionTemplate(manager()); }
        @Bean AbstractPlatformTransactionManager manager() {
            return new AbstractPlatformTransactionManager() {
                @Override protected Object doGetTransaction() { return new Object(); }
                @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
                @Override protected void doCommit(DefaultTransactionStatus status) { }
                @Override protected void doRollback(DefaultTransactionStatus status) { }
            };
        }
    }
}
