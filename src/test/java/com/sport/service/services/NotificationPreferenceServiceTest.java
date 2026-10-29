package com.sport.service.services;

import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.subscriber.RoleType;
import com.sport.service.repositories.SubscriberRepository;
import com.sport.service.store.notifications.Notification;
import com.sport.service.store.notifications.NotificationPublisher;
import com.sport.service.web.models.settings.NotificationPreferences;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationPreferenceServiceTest {
    private final SubscriberRepository repository = mock(SubscriberRepository.class);
    private final NotificationPreferenceService service = new NotificationPreferenceService(repository);

    @Test
    void preferencesAreIndependentAndDoNotChangeProfileOrRole() {
        Subscriber user = Subscriber.builder().id(10L).username("user").role(RoleType.SUBSCRIBER)
                .getWeather(true).getEvents(false).getPlaces(true).getCoaches(false).build();
        when(repository.findById(10L)).thenReturn(Optional.of(user));
        assertEquals(new NotificationPreferences(true, false, true, false, false), service.get(10L));
        var next = new NotificationPreferences(false, true, false, true, true);
        assertEquals(next, service.update(10L, next));
        assertEquals(next, service.get(10L));
        assertEquals("user", user.getUsername());
        assertEquals(RoleType.SUBSCRIBER, user.getRole());
        verify(repository).save(user);
    }

    @ParameterizedTest
    @EnumSource(value = Notification.NotificationType.class, names = {"WEATHER", "EVENT", "PLACE", "COACH", "JOINT_TRAINING"})
    void eachCategoryUsesItsOwnSubscription(Notification.NotificationType type) {
        Subscriber user = Subscriber.builder().id(10L).getWeather(false).getEvents(false)
                .getPlaces(false).getCoaches(false).build();
        when(repository.findById(10L)).thenReturn(Optional.of(user));
        assertFalse(service.isEnabled(10L, type));
        switch (type) {
            case WEATHER -> { user.setGetWeather(true); when(repository.findAllByGetWeatherTrue()).thenReturn(List.of(user)); }
            case EVENT -> { user.setGetEvents(true); when(repository.findAllByGetEventsTrue()).thenReturn(List.of(user)); }
            case PLACE -> { user.setGetPlaces(true); when(repository.findAllByGetPlacesTrue()).thenReturn(List.of(user)); }
            case COACH -> { user.setGetCoaches(true); when(repository.findAllByGetCoachesTrue()).thenReturn(List.of(user)); }
            case JOINT_TRAINING -> { user.setGetJointTrainings(true); when(repository.findAllByGetJointTrainingsTrue()).thenReturn(List.of(user)); }
            default -> fail();
        }
        assertTrue(service.isEnabled(10L, type));
        assertEquals(List.of(10L), service.recipients(type));
    }

    @Test
    void adminBroadcastIsNeverFilteredAndHasDistinctType() {
        assertTrue(service.isEnabled(10L, Notification.NotificationType.ADMIN_TO_SUBSCRIBER));
        verifyNoInteractions(repository);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        var sender = new NotificationSenderService(publisher);
        byte[] photo = {1};
        sender.sendAdminToSubscriberNotification("Всем", photo, List.of(10L, 20L));
        ArgumentCaptor<Notification> sent = ArgumentCaptor.forClass(Notification.class);
        verify(publisher).publishToUsers(eq(List.of(10L, 20L)), sent.capture());
        assertEquals(Notification.NotificationType.ADMIN_TO_SUBSCRIBER, sent.getValue().getType());
        assertArrayEquals(photo, sent.getValue().getPhoto());
    }
}
