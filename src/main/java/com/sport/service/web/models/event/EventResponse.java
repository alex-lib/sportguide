package com.sport.service.web.models.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
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
