package com.sport.service.web.controllers;

import com.sport.service.services.EventService;
import com.sport.service.web.models.event.ListEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListEventResponse getAllEvents(
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String date,
        @RequestParam(required = false) String search
    ) {
        return eventService.findAllEventsWithFilter(district, date, search);
    }
}
