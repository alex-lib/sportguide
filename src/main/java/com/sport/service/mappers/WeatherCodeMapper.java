package com.sport.service.mappers;

import com.sport.service.bot.constants.WeatherConstants;

import java.util.HashMap;
import java.util.Map;

public class WeatherCodeMapper {
    private static final Map<Integer, String> WEATHER_DESCRIPTIONS = new HashMap<>();

    static {
        WEATHER_DESCRIPTIONS.put(0, WeatherConstants.CLEAR);
        WEATHER_DESCRIPTIONS.put(1, WeatherConstants.MOSTLY_CLEAR);
        WEATHER_DESCRIPTIONS.put(2, WeatherConstants.PARTLY_CLOUDY);
        WEATHER_DESCRIPTIONS.put(3, WeatherConstants.CLOUDY);

        WEATHER_DESCRIPTIONS.put(45, WeatherConstants.FOG);
        WEATHER_DESCRIPTIONS.put(48, WeatherConstants.FOG);

        for (int code = 51; code <= 55; code++) {
            WEATHER_DESCRIPTIONS.put(code, WeatherConstants.DRIZZLE);
        }

        WEATHER_DESCRIPTIONS.put(56, WeatherConstants.ICY_DRIZZLE);
        WEATHER_DESCRIPTIONS.put(57, WeatherConstants.ICY_DRIZZLE);

        for (int code = 61; code <= 65; code++) {
            WEATHER_DESCRIPTIONS.put(code, WeatherConstants.RAIN);
        }

        WEATHER_DESCRIPTIONS.put(66, WeatherConstants.FREEZING_RAIN);
        WEATHER_DESCRIPTIONS.put(67, WeatherConstants.FREEZING_RAIN);

        for (int code = 71; code <= 75; code++) {
            WEATHER_DESCRIPTIONS.put(code, WeatherConstants.SNOW);
        }

        WEATHER_DESCRIPTIONS.put(77, WeatherConstants.SNOW);

        WEATHER_DESCRIPTIONS.put(80, WeatherConstants.SHOWERS);
        WEATHER_DESCRIPTIONS.put(81, WeatherConstants.SHOWERS);
        WEATHER_DESCRIPTIONS.put(82, WeatherConstants.SHOWERS);

        WEATHER_DESCRIPTIONS.put(85, WeatherConstants.SNOWFALL);
        WEATHER_DESCRIPTIONS.put(86, WeatherConstants.SNOWFALL);

        WEATHER_DESCRIPTIONS.put(95, WeatherConstants.THUNDERSTORM);

        WEATHER_DESCRIPTIONS.put(96, WeatherConstants.THUNDERSTORM_WITH_HAIL);
        WEATHER_DESCRIPTIONS.put(99, WeatherConstants.THUNDERSTORM_WITH_HAIL);
    }

    public static String mapCodeToWeatherDescription(int code) {
        return WEATHER_DESCRIPTIONS.getOrDefault(code, WeatherConstants.UNKNOWN);
    }
}