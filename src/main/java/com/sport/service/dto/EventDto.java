package com.sport.service.dto;
import com.sport.service.entities.place.District;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {

    private String name;

    private String address;

    private String description;

    private String link;

    private String placeName;

    private String date;

    private String time;

    private District district;

    private int step = 0;
}