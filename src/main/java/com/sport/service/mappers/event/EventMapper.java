package com.sport.service.mappers.event;
import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@DecoratedWith(EventMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    Event eventDtoToEvent(EventDto dto);
}