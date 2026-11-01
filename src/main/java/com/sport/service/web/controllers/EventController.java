package com.sport.service.web.controllers;

import com.sport.service.services.EventService;
import com.sport.service.web.models.event.EventEditRequest;
import com.sport.service.web.models.event.EventEditResponse;
import com.sport.service.web.models.event.ListEventResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    public Map<String, Boolean> permissions(Authentication authentication) {
        return Map.of(
                "canEdit",
                authentication.getAuthorities().stream()
                        .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EventEditResponse getForEditing(@PathVariable Long id) {
        return eventService.getForEditing(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EventEditResponse update(
            @PathVariable Long id, @Valid @RequestBody EventEditRequest request) {
        return eventService.updateEvent(id, request);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListEventResponse getAllEvents(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search) {
        return eventService.findAllEventsWithFilter(district, date, search);
    }
}
