package com.sport.service.services.impl;

import com.sport.service.api.OpenMeteoClient;
import com.sport.service.entities.TodayWeather;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.events.EventCreatedTodayWeather;
import com.sport.service.mappers.WeatherCodeMapper;
import com.sport.service.services.SubscriberService;
import com.sport.service.services.WeatherService;
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

        String message = String.format(
                "Доброго утра!\n" +
                        "\n🌍 Погода в Воронеже на сегодня (%s):\n" +
                        "🌡️ Сейчас: %s ~ %.1f°C\n" +
                        "📊 Экстремумы: макс. ~ %.1f°C, мин. ~ %.1f°C\n" +
                        "⏱️ По времени:\n" +
                        "9:00: %s ~ %.1f°C, вероятность осадков: %d%%\n" +
                        "12:00: %s ~ %.1f°C, вероятность осадков: %d%%\n" +
                        "15:00: %s ~ %.1f°C, вероятность осадков: %d%%\n" +
                        "18:00: %s ~ %.1f°C, вероятность осадков: %d%%\n" +
                        "21:00: %s ~ %.1f°C, вероятность осадков: %d%%\n" +
                        "\nУ природы нет, для тренировки, плохой погоды!\n" +
                        "#погода",

                weather.getDate(),
                weather.getCurrent().getDescription(),
                weather.getCurrent().getTemperature(),

                weather.getDaily().getMaxTemperature(),
                weather.getDaily().getMinTemperature(),

                weather.getHourlyForecast().get(9).getDescription(),
                weather.getHourlyForecast().get(9).getTemperature(),
                weather.getHourlyForecast().get(9).getPrecipitationProbability(),

                weather.getHourlyForecast().get(12).getDescription(),
                weather.getHourlyForecast().get(12).getTemperature(),
                weather.getHourlyForecast().get(12).getPrecipitationProbability(),

                weather.getHourlyForecast().get(15).getDescription(),
                weather.getHourlyForecast().get(15).getTemperature(),
                weather.getHourlyForecast().get(15).getPrecipitationProbability(),

                weather.getHourlyForecast().get(18).getDescription(),
                weather.getHourlyForecast().get(18).getTemperature(),
                weather.getHourlyForecast().get(18).getPrecipitationProbability(),

                weather.getHourlyForecast().get(21).getDescription(),
                weather.getHourlyForecast().get(21).getTemperature(),
                weather.getHourlyForecast().get(21).getPrecipitationProbability());

        List<Subscriber> subscribers = subscriberService.getSubscribersWhoWantGetEvents();
        eventPublisher.publishEvent(new EventCreatedTodayWeather(subscribers, message));
        return message;
    }

    private TodayWeather convertToTodayWeather(OpenMeteoResponse response) {
        TodayWeather.CurrentWeather current = TodayWeather.CurrentWeather.builder()
                .temperature(response.getCurrent().getTemperature_2m())
                .description(WeatherCodeMapper.getWeatherDescription(
                        response.getCurrent().getWeathercode()))
                .build();

        List<TodayWeather.HourlyForecast> hourly = new ArrayList<>();
        if (response.getHourly() != null && response.getHourly().getTime() != null) {
            for (int i = 0; i < Math.min(24, response.getHourly().getTime().size()); i++) {
                hourly.add(TodayWeather.HourlyForecast.builder()
                        .time(response.getHourly().getTime().get(i))
                        .temperature(response.getHourly().getTemperature_2m().get(i))
                        .description(WeatherCodeMapper.getWeatherDescription(
                                response.getHourly().getWeathercode().get(i)))
                        .precipitationProbability(response.getHourly().getPrecipitation_probability().get(i))
                        .build());
            }
        }

        TodayWeather.DailySummary daily = TodayWeather.DailySummary.builder()
                .maxTemperature(response.getDaily().getTemperature_2m_max().get(0))
                .minTemperature(response.getDaily().getTemperature_2m_min().get(0))
                .description(WeatherCodeMapper.getWeatherDescription(
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