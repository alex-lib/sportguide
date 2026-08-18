package com.sport.service.web.controllers;

import com.sport.service.services.PlaceService;
import com.sport.service.web.models.place.ListPlaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/places")
@RequiredArgsConstructor
@Slf4j
public class PlaceController {
    private final PlaceService placeService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListPlaceResponse getAllPlaces(
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String subDistrict,
        @RequestParam(required = false) String outdoor,
        @RequestParam(required = false) String placeType,
        @RequestParam(required = false) String search
    ) {
        log.info("[API] GET /api/places | district={}, subDistrict={}, outdoor={}, placeType={}, search={}",
                district, subDistrict, outdoor, placeType, search);
        return placeService.findAll(district, subDistrict, outdoor, placeType, search);
    }
}
