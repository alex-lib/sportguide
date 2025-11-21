package com.sport.service.mappers.event;

import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import com.sport.service.mappers.DistrictStringMapper;
import com.sport.service.web.models.event.EventResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public abstract class EventMapperDelegate implements EventMapper {

    @Override
    public Event eventDtoToEvent(EventDto dto) {
        return Event.builder()
                .district(dto.getDistrict())
                .name(dto.getName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .placeName(dto.getPlaceName())
                .link(dto.getLink())
                .date(LocalDate.parse(dto.getDate()))
                .time(LocalTime.parse(dto.getTime()))
                .build();
    }

    @Override
    public List<EventResponse> ListEventToListEventResponse(List<Event> events) {
        List<EventResponse> placeResponses = new ArrayList<>();
        for (Event event : events) {
            String districtString = DistrictStringMapper.districtEnumToDistrictString(event.getDistrict());
            String coordinates;
            if (event.getCoordinates() != null) {
                String[] coordinatesArray = event.getCoordinates().split(",");
                float latitude = Float.parseFloat(coordinatesArray[0].trim());
                float longitude = Float.parseFloat(coordinatesArray[1].trim());
                coordinates = String.format("https://maps.google.com/?q=%f,%f", latitude, longitude);
            } else {
                coordinates = "Координаты места не указаны";
            }
            placeResponses.add(new EventResponse(
                    event.getName(),
                    event.getDescription(),
                    event.getPlaceName(),
                    districtString,
                    event.getAddress(),
                    event.getLink(),
                    event.getDate().toString(),
                    event.getTime().toString(),
                    coordinates));
        }
        return placeResponses;
    }
}