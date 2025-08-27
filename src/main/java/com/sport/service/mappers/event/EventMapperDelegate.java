package com.sport.service.mappers.event;
import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import java.time.LocalDate;

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
                .time(dto.getTime())
                .build();
    }
}