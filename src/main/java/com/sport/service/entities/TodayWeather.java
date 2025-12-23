package com.sport.service.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TodayWeather {
    private String date;
    private CurrentWeather current;
    private List<HourlyForecast> hourlyForecast;
    private DailySummary daily;

    @Data
    @Builder
    public static class CurrentWeather {
        private Double temperature;
        private String description;
    }

    @Data
    @Builder
    public static class HourlyForecast {
        private String time;
        private Double temperature;
        private String description;
        private Integer precipitationProbability;
    }

    @Data
    @Builder
    public static class DailySummary {
        private Double maxTemperature;
        private Double minTemperature;
        private String description;
    }
}