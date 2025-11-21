package com.sport.service.web.controllers

import com.sport.service.mappers.coach.CoachMapper
import com.sport.service.services.CoachService
import com.sport.service.web.models.coach.CoachFilter
import com.sport.service.web.models.coach.CoachResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/events")
@RestController
class CoachController(
    private val coachService: CoachService,
    private val coachMapper: CoachMapper
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    fun findAll(filter: CoachFilter): List<CoachResponse> {
        return coachMapper.ListCoachToListCoachResponseList(coachService.findAll(filter));
    }
}