package com.sport.service.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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

    private int step = 0;
}