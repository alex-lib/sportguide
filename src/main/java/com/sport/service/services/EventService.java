package com.sport.service.services;

import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import com.sport.service.web.models.event.EventFilter;
import com.sport.service.web.models.event.ListEventResponse;

import java.util.List;

public interface EventService {

    void create(EventDto dto);

    void deleteByName(String eventName);

    List<Event> findAll();

    void deleteByExpiredDate();

    boolean existsByName(String eventName);

    ListEventResponse findAll(EventFilter filter);
}