package com.sport.service.services;

import com.sport.service.dto.WeatherDataAtSpecificHourDto;
import com.sport.service.entities.Event;
import com.sport.service.entities.JointTraining;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.TodayWeather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationCreatorService {
    private final TemplateEngine templateEngine;

    public String createWeatherNotification(TodayWeather weather) {
        Context context = new Context();

        context.setVariable("date", weather.getDate());
        context.setVariable("currentDescription", weather.getCurrent().getDescription());
        context.setVariable("currentTemperature", String.format("%.1f", weather.getCurrent().getTemperature()));
        context.setVariable("maxTemperature", String.format("%.1f", weather.getDaily().getMaxTemperature()));
        context.setVariable("minTemperature", String.format("%.1f", weather.getDaily().getMinTemperature()));

        List<WeatherDataAtSpecificHourDto> weatherHourlyInfo = new ArrayList<>();
        for (int i = 9; i <= 21; i = i + 3) {
            WeatherDataAtSpecificHourDto weatherDataAtSpecificHourDto = WeatherDataAtSpecificHourDto.builder()
                    .time(i + ":00")
                    .description(weather.getHourlyForecast().get(i).getDescription())
                    .temperature(String.format("%.1f", weather.getHourlyForecast().get(i).getTemperature()))
                    .precipitationProbability(weather.getHourlyForecast().get(i).getPrecipitationProbability())
                    .build();
            weatherHourlyInfo.add(weatherDataAtSpecificHourDto);
        }

        context.setVariable("weatherHourlyInfo", weatherHourlyInfo);
        return templateEngine.process("weather_notification.txt", context);
    }

    public String createEventNotification(Event event) {
        Context context = new Context();

        context.setVariable("eventName", event.getName());
        context.setVariable("eventDescription", event.getDescription());
        context.setVariable("eventDate", event.getDate());
        context.setVariable("eventTime", event.getTime());
        context.setVariable("eventDistrict", event.getDistrict());
        context.setVariable("eventAddress", event.getAddress());
        context.setVariable("eventPlaceName", event.getPlaceName());
        context.setVariable("eventLink", event.getLink());

        return templateEngine.process("event_notification.txt", context);
    }

    public String createSubscriberSentMessageToAdminNotification(String text, User user) {
        Context context = new Context();

        context.setVariable("username", user.getUserName());
        context.setVariable("userId", user.getId());
        context.setVariable("message", text);
        return templateEngine.process("subscriber_sent_message_to_admin_notification.txt", context);
    }

    public String createPlaceMessage(Place place, String mapLink) {
        Context context = new Context();

        context.setVariable("place", place);
        context.setVariable("mapLink", mapLink);

        if (place.getWebSite() == null || place.getWebSite().equals("-")) {
            return templateEngine.process("place_without_link_message.txt", context);
        }

        return templateEngine.process("place_with_link_message.txt", context);
    }

    public String createRequestToApproveJointTraining(JointTraining jointTraining) {
        Context context = new Context();

        context.setVariable("jointTraining", jointTraining);
        return templateEngine.process("request_to_approve_joint_training.txt", context);
    }

    public String createNewUserAlert(Subscriber subscriber) {
        Context context = new Context();

        context.setVariable("id", subscriber.id);
        context.setVariable("username", subscriber.username);
        context.setVariable("firstName", subscriber.firstName);
        context.setVariable("lastName", subscriber.lastName);
        context.setVariable("getEvents", subscriber.getEvents);
        context.setVariable("registrationDate", java.time.LocalDateTime.now());
        return templateEngine.process("new_user.txt", context);
    }

    public String createRedisDownAlert(java.time.LocalDateTime timestamp) {
        Context context = new Context();
        context.setVariable("timestamp", timestamp);
        return templateEngine.process("redis_down_alert.txt", context);
    }

    public String createRedisDownReminderAlert(long minutesSinceLastAlert) {
        Context context = new Context();
        context.setVariable("minutesSinceLastAlert", minutesSinceLastAlert);
        context.setVariable("timestamp", java.time.LocalDateTime.now());
        return templateEngine.process("redis_down_reminder_alert.txt", context);
    }

    public String createRedisUpAlert(java.time.LocalDateTime timestamp) {
        Context context = new Context();
        context.setVariable("timestamp", timestamp);
        return templateEngine.process("redis_up_alert.txt", context);
    }
}