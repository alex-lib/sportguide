//package com.sport.service.web.controllers
//
//import com.sport.service.services.EventService
//import com.sport.service.web.models.event.EventFilter
//import com.sport.service.web.models.event.ListEventResponse
//import org.springframework.security.access.prepost.PreAuthorize
//import org.springframework.web.bind.annotation.GetMapping
//
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/events")
//class EventController(
//    private val eventService: EventService,
//) {
//
//    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
//    @GetMapping
//    fun findAll(filter: EventFilter): ListEventResponse {
//        return eventService.findAll(filter)
//    }
//}