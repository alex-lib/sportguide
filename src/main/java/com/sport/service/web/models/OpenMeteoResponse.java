package com.sport.service.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse {
    private Double latitude;
    private Double longitude;
    private String timezone;
    private Current current;
    private Hourly hourly;
    private Daily daily;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        private Double temperature_2m;
        private Integer weathercode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hourly {
        private List<String> time;
        private List<Double> temperature_2m;
        private List<Integer> weathercode;
        private List<Integer> precipitation_probability;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Daily {
        private List<String> time;
        private List<Integer> weathercode;
        private List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;
    }
}