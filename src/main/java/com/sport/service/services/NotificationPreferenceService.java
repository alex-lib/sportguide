package com.sport.service.services;

import com.sport.service.entities.Subscriber;
import com.sport.service.repositories.SubscriberRepository;
import com.sport.service.store.notifications.Notification.NotificationType;
import com.sport.service.web.models.settings.NotificationPreferences;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {
    private final SubscriberRepository subscribers;

    @Transactional(readOnly = true)
    public NotificationPreferences get(long userId) {
        Subscriber user = find(userId);
        return new NotificationPreferences(Boolean.TRUE.equals(user.getGetWeather()),
                Boolean.TRUE.equals(user.getGetEvents()), Boolean.TRUE.equals(user.getGetPlaces()),
                Boolean.TRUE.equals(user.getGetCoaches()), Boolean.TRUE.equals(user.getGetJointTrainings()));
    }

    @Transactional
    public NotificationPreferences update(long userId, NotificationPreferences preferences) {
        Subscriber user = find(userId);
        user.setGetWeather(preferences.weather());
        user.setGetEvents(preferences.events());
        user.setGetPlaces(preferences.places());
        user.setGetCoaches(preferences.coaches());
        user.setGetJointTrainings(preferences.jointTrainings());
        subscribers.save(user);
        return preferences;
    }

    @Transactional(readOnly = true)
    public List<Long> recipients(NotificationType type) {
        List<Subscriber> users = switch (type) {
            case WEATHER -> subscribers.findAllByGetWeatherTrue();
            case EVENT -> subscribers.findAllByGetEventsTrue();
            case PLACE -> subscribers.findAllByGetPlacesTrue();
            case COACH -> subscribers.findAllByGetCoachesTrue();
            case JOINT_TRAINING -> subscribers.findAllByGetJointTrainingsTrue();
            default -> throw new IllegalArgumentException("Not a subscription notification: " + type);
        };
        return users.stream().map(Subscriber::getId).toList();
    }

    @Transactional(readOnly = true)
    public boolean isEnabled(long userId, NotificationType type) {
        if (type != NotificationType.WEATHER && type != NotificationType.EVENT
                && type != NotificationType.PLACE && type != NotificationType.COACH
                && type != NotificationType.JOINT_TRAINING) return true;
        return subscribers.findById(userId).map(user -> switch (type) {
            case WEATHER -> Boolean.TRUE.equals(user.getGetWeather());
            case EVENT -> Boolean.TRUE.equals(user.getGetEvents());
            case PLACE -> Boolean.TRUE.equals(user.getGetPlaces());
            case COACH -> Boolean.TRUE.equals(user.getGetCoaches());
            case JOINT_TRAINING -> Boolean.TRUE.equals(user.getGetJointTrainings());
            default -> true;
        }).orElse(false);
    }

    private Subscriber find(long userId) {
        return subscribers.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }
}
