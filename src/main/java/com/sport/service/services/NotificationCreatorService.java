package com.sport.service.services;

import com.sport.service.entities.Event;
import com.sport.service.entities.Place;
import com.sport.service.entities.TodayWeather;
import org.telegram.telegrambots.meta.api.objects.User;

public interface NotificationCreatorService {
    String createWeatherNotification(TodayWeather weather);

    String createEventNotification(Event event);

    String createSubscriberSentMessageToAdminNotification(String text, User user);

    String createPlaceMessage(Place place, String mapLink);
}