package com.sport.service.mappers;

import java.util.HashMap;
import java.util.Map;

public class WeatherCodeMapper {
    private static final Map<Integer, String> WEATHER_DESCRIPTIONS = new HashMap<>();

    static {
        WEATHER_DESCRIPTIONS.put(0, "Ясно☀️");
        WEATHER_DESCRIPTIONS.put(1, "Преимущественно ясно🌤️");
        WEATHER_DESCRIPTIONS.put(2, "Переменная облачность⛅");
        WEATHER_DESCRIPTIONS.put(3, "Пасмурно☁️");

        WEATHER_DESCRIPTIONS.put(45, "Туман🌫️");
        WEATHER_DESCRIPTIONS.put(48, "Туман🌫️");

        for (int code = 51; code <= 55; code++) {
            WEATHER_DESCRIPTIONS.put(code, "Морось🌧️");
        }

        WEATHER_DESCRIPTIONS.put(56, "Ледяная морось🌧️");
        WEATHER_DESCRIPTIONS.put(57, "Ледяная морось🌧️");

        for (int code = 61; code <= 65; code++) {
            WEATHER_DESCRIPTIONS.put(code, "Дождь🌧️");
        }

        WEATHER_DESCRIPTIONS.put(66, "Ледяной дождь🌧️");
        WEATHER_DESCRIPTIONS.put(67, "Ледяной дождь🌧️");

        for (int code = 71; code <= 75; code++) {
            WEATHER_DESCRIPTIONS.put(code, "Снег❄️");
        }

        WEATHER_DESCRIPTIONS.put(77, "Град🌨️");

        WEATHER_DESCRIPTIONS.put(80, "Ливни⛈️");
        WEATHER_DESCRIPTIONS.put(81, "Ливни⛈️");
        WEATHER_DESCRIPTIONS.put(82, "Ливни⛈️");

        WEATHER_DESCRIPTIONS.put(85, "Снегопад🌨️");
        WEATHER_DESCRIPTIONS.put(86, "Снегопад🌨️");

        WEATHER_DESCRIPTIONS.put(95, "Гроза⛈️");

        WEATHER_DESCRIPTIONS.put(96, "Гроза с градом⛈️");
        WEATHER_DESCRIPTIONS.put(99, "Гроза с градом⛈️");
    }

    public static String mapCodeToWeatherDescription(int code) {
        return WEATHER_DESCRIPTIONS.getOrDefault(code, "Неизвестно");
    }
}