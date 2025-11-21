package com.sport.service.web.controllers

import com.sport.service.mappers.place.PlaceMapper
import com.sport.service.services.PlaceService
import com.sport.service.web.models.place.PlaceFilter
import com.sport.service.web.models.place.PlaceResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/places")
class PlaceController(
    private val placeService: PlaceService,
    private val placeMapper: PlaceMapper
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    fun findAll(@Valid filter: PlaceFilter): List<PlaceResponse> {
        return placeMapper.ListPlaceToListPlaceResponse(placeService.findAll(filter));
    }
}