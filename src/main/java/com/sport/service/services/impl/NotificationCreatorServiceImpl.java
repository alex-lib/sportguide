package com.sport.service.services.impl;

import com.sport.service.dto.WeatherDataAtSpecificHourDto;
import com.sport.service.entities.Event;
import com.sport.service.entities.TodayWeather;
import com.sport.service.services.NotificationCreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationCreatorServiceImpl implements NotificationCreatorService {
    private final TemplateEngine templateEngine;

    @Override
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

    @Override
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

    @Override
    public String createSubscriberSentMessageToAdminNotification(String text, User user) {
        Context context = new Context();

        context.setVariable("username", user.getUserName());
        context.setVariable("userId", user.getId());
        context.setVariable("message", text);
        return templateEngine.process("subscriber_sent_message_to_admin_notification.txt", context);
    }
}