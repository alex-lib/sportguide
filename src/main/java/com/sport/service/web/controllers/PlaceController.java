package com.sport.service.web.controllers;

import com.sport.service.services.PlaceService;
import com.sport.service.web.models.place.ListPlaceResponse;
import com.sport.service.web.models.place.PlaceFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListPlaceResponse getAllPlaces(@Valid PlaceFilter filter) {
        return placeService.findAll(filter);
    }
}