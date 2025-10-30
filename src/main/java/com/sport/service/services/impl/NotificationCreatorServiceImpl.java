package com.sport.service.services.impl;

import com.sport.service.entities.Event;
import com.sport.service.entities.TodayWeather;
import com.sport.service.services.NotificationCreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

        context.setVariable("time9Description", weather.getHourlyForecast().get(9).getDescription());
        context.setVariable("time9Temperature", String.format("%.1f", weather.getHourlyForecast().get(9).getTemperature()));
        context.setVariable("time9Precipitation", weather.getHourlyForecast().get(9).getPrecipitationProbability());

        context.setVariable("time12Description", weather.getHourlyForecast().get(12).getDescription());
        context.setVariable("time12Temperature", String.format("%.1f", weather.getHourlyForecast().get(12).getTemperature()));
        context.setVariable("time12Precipitation", weather.getHourlyForecast().get(12).getPrecipitationProbability());

        context.setVariable("time15Description", weather.getHourlyForecast().get(15).getDescription());
        context.setVariable("time15Temperature", String.format("%.1f", weather.getHourlyForecast().get(15).getTemperature()));
        context.setVariable("time15Precipitation", weather.getHourlyForecast().get(15).getPrecipitationProbability());

        context.setVariable("time18Description", weather.getHourlyForecast().get(18).getDescription());
        context.setVariable("time18Temperature", String.format("%.1f", weather.getHourlyForecast().get(18).getTemperature()));
        context.setVariable("time18Precipitation", weather.getHourlyForecast().get(18).getPrecipitationProbability());

        context.setVariable("time21Description", weather.getHourlyForecast().get(21).getDescription());
        context.setVariable("time21Temperature", String.format("%.1f", weather.getHourlyForecast().get(21).getTemperature()));
        context.setVariable("time21Precipitation", weather.getHourlyForecast().get(21).getPrecipitationProbability());

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