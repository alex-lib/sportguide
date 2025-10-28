package com.sport.service.api;

import com.sport.service.web.models.OpenMeteoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "open-meteo-client", url = "https://api.open-meteo.com")
public interface OpenMeteoClient {

    @GetMapping("/v1/forecast")
    OpenMeteoResponse getTodayWeather(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam String hourly,
            @RequestParam String daily,
            @RequestParam String current,
            @RequestParam String timezone,
            @RequestParam String forecast_days);
}