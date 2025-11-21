package com.sport.service.web.controllers

import com.sport.service.mappers.event.EventMapper
import com.sport.service.services.EventService
import com.sport.service.web.models.event.EventFilter
import com.sport.service.web.models.event.EventResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class EventController(
    private val eventService: EventService,
    private val eventMapper: EventMapper
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    fun findAll(filter: EventFilter): List<EventResponse> {
        return eventMapper.ListEventToListEventResponse(eventService.findAll(filter));
    }
}