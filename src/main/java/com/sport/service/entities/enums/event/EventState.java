package com.sport.service.entities.enums.event;

import com.sport.service.entities.enums.common.District;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventState {
    private District district;
    private String name;
    private String address;
    private String description;
    private String link;
    private String placeName;
    private String date;
    private String time;
    private CreateEventStep step;
}
