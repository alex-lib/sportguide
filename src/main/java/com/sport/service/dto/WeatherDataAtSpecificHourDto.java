package com.sport.service.dto;

import lombok.*;

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