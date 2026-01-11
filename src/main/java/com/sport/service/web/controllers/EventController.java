package com.sport.service.web.controllers;

import com.sport.service.services.EventService;
import com.sport.service.web.models.event.EventFilter;
import com.sport.service.web.models.event.ListEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListEventResponse getAllEvents(EventFilter filter) {
        return eventService.findAllEventsWithFilter(filter);
    }
}