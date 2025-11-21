package com.sport.service.web.controllers

import com.sport.service.dto.CoachDto
import com.sport.service.services.CoachService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private var coachService: CoachService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    fun test(): String {
        return "Test, look here!"
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/coach")
    fun createCoach(dto: CoachDto) {
        coachService.create(dto)
    }
}