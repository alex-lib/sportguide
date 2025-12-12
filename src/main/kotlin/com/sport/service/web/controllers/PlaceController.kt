//package com.sport.service.web.controllers
//
//import com.sport.service.services.PlaceService
//import com.sport.service.web.models.place.ListPlaceResponse
//import com.sport.service.web.models.place.PlaceFilter
//import jakarta.validation.Valid
//import org.springframework.security.access.prepost.PreAuthorize
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/places")
//class PlaceController(
//    private val placeService: PlaceService,
//) {
//
//    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
//    @GetMapping
//    fun findAll(@Valid filter: PlaceFilter): ListPlaceResponse {
//        return placeService.findAll(filter)
//    }
//}