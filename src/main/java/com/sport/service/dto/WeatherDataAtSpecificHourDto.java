package com.sport.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeatherDataAtSpecificHourDto {
    private String time;
    private String description;
    private String temperature;
    private Integer precipitationProbability;
}