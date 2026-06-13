package com.sport.service.services;

import com.sport.service.constants.Constants;
import com.sport.service.entities.TodayWeather;
import com.sport.service.mappers.WeatherCodeMapper;
import com.sport.service.web.api.OpenMeteoClient;
import com.sport.service.web.models.OpenMeteoResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenMeteoClientService {
    private final OpenMeteoClient openMeteoClient;

    @Retryable(
            retryFor = { FeignException.class, IOException.class },
            maxAttempts = 5,
            backoff = @Backoff(
                    delay = 10_000,
                    multiplier = 2,
                    maxDelay = 300_000
            )
    )
    public TodayWeather getTodayWeather() {
        OpenMeteoResponse response = openMeteoClient.getTodayWeather(
                Constants.COORDINATES[0],
                Constants.COORDINATES[1],
                "temperature_2m,weathercode,precipitation_probability",
                "weathercode,temperature_2m_max,temperature_2m_min",
                "temperature_2m,weathercode",
                Constants.TIME_ZONE,
                "1");
        return convertToTodayWeather(response);
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
