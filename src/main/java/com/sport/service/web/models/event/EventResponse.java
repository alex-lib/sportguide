package com.sport.service.web.models.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String name;
    private String description;
    private String placeName;
    private String district;
    private String address;
    private String link;
    private String date;
    private String time;
    private String coordinates;
}