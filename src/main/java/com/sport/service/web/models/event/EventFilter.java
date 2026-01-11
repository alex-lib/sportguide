package com.sport.service.web.models.event;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventFilter {
    private String district;
    private String date;
}