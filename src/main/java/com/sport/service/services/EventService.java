package com.sport.service.services;
import com.sport.service.entities.Event;

import java.util.List;

public interface EventService {

    void create(Event event);

    void deleteByName(String eventName);

    List<Event> findAll();

    void deleteByExpiredDate();
}