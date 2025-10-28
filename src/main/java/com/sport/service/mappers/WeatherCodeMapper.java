package com.sport.service.mappers;

public class WeatherCodeMapper {

    public static String getWeatherDescription(int code) {
        if (code == 0) return "Ясно☀️";
        if (code == 1) return "Преимущественно ясно🌤️";
        if (code == 2) return "Переменная облачность⛅";
        if (code == 3) return "Пасмурно☁️";
        if (code == 45 || code == 48) return "Туман🌫️";
        if (code >= 51 && code <= 55) return "Морось🌧️";
        if (code == 56 || code == 57) return "Ледяная морось🌧️";
        if (code >= 61 && code <= 65) return "Дождь🌧️";
        if (code == 66 || code == 67) return "Ледяной дождь🌧️";
        if (code >= 71 && code <= 75) return "Снег❄️";
        if (code == 77) return "Град🌨️";
        if (code >= 80 && code <= 82) return "Ливни⛈️";
        if (code == 85 || code == 86) return "Снегопад🌨️";
        if (code == 95) return "Гроза⛈️";
        if (code == 96 || code == 99) return "Гроза с градом⛈️";
        return "Неизвестно";
    }
}