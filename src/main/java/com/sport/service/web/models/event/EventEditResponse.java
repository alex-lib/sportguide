package com.sport.service.web.models.event;

import com.sport.service.entities.Event;
import com.sport.service.entities.enums.common.District;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventEditResponse(
        Long id,
        Long version,
        String name,
        String description,
        LocalDate date,
        LocalTime time,
        District district,
        String address,
        String placeName,
        String link,
        String coordinates) {
    public static EventEditResponse from(Event event) {
        return new EventEditResponse(
                event.getId(),
                event.getVersion(),
                event.getName(),
                event.getDescription(),
                event.getDate(),
                event.getTime(),
                event.getDistrict(),
                event.getAddress(),
                event.getPlaceName(),
                event.getLink(),
                event.getCoordinates());
    }
}
