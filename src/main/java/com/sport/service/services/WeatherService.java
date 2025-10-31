package com.sport.service.services;

import com.sport.service.entities.TodayWeather;

public interface WeatherService {
    TodayWeather getTodayWeather();

    String createWeatherNotification();
}