package com.sport.service.services.impl;

import com.sport.service.entities.TodayWeather;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.events.WeatherNotificationCreatedEvent;
import com.sport.service.mappers.WeatherCodeMapper;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.services.SubscriberService;
import com.sport.service.services.WeatherService;
import com.sport.service.web.api.OpenMeteoClient;
import com.sport.service.web.models.OpenMeteoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {
    private final double[] coordinates = {51.694235, 39.227656};
    private static final String CRON = "0 0 6 * * *";

    private final OpenMeteoClient openMeteoClient;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscriberService subscriberService;
    private final NotificationCreatorService notificationCreatorService;

    @Override
    public TodayWeather getTodayWeather() {
        OpenMeteoResponse response = openMeteoClient.getTodayWeather(
                coordinates[0],
                coordinates[1],
                "temperature_2m,weathercode,precipitation_probability",
                "weathercode,temperature_2m_max,temperature_2m_min",
                "temperature_2m,weathercode",
                "Europe/Moscow",
                "1");
        return convertToTodayWeather(response);
    }

    @Override
    @Scheduled(cron = CRON, zone = "Europe/Moscow")
    public String createWeatherNotification() {
        TodayWeather weather = getTodayWeather();
        String message = notificationCreatorService.createWeatherNotification(weather);
        List<Subscriber> subscribers = subscriberService.getSubscribersWhoWantGetEvents();
        eventPublisher.publishEvent(new WeatherNotificationCreatedEvent(subscribers, message));
        return message;
    }

    private TodayWeather convertToTodayWeather(OpenMeteoResponse response) {
        TodayWeather.CurrentWeather current = TodayWeather.CurrentWeather.builder()
                .temperature(response.getCurrent().getTemperature_2m())
                .description(WeatherCodeMapper.mapCodeToWeatherDescription(
                        response.getCurrent().getWeathercode()))
                .build();

        List<TodayWeather.HourlyForecast> hourly = new ArrayList<>();
        if (response.getHourly() != null && response.getHourly().getTime() != null) {
            for (int i = 0; i < Math.min(24, response.getHourly().getTime().size()); i++) {
                hourly.add(TodayWeather.HourlyForecast.builder()
                        .time(response.getHourly().getTime().get(i))
                        .temperature(response.getHourly().getTemperature_2m().get(i))
                        .description(WeatherCodeMapper.mapCodeToWeatherDescription(
                                response.getHourly().getWeathercode().get(i)))
                        .precipitationProbability(response.getHourly().getPrecipitation_probability().get(i))
                        .build());
            }
        }

        TodayWeather.DailySummary daily = TodayWeather.DailySummary.builder()
                .maxTemperature(response.getDaily().getTemperature_2m_max().get(0))
                .minTemperature(response.getDaily().getTemperature_2m_min().get(0))
                .description(WeatherCodeMapper.mapCodeToWeatherDescription(
                        response.getDaily().getWeathercode().get(0)))
                .build();

        return TodayWeather.builder()
                .date(response.getDaily().getTime().get(0))
                .current(current)
                .hourlyForecast(hourly)
                .daily(daily)
                .build();
    }
}