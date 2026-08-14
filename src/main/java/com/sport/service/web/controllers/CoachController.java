package com.sport.service.web.controllers;

import com.sport.service.services.CoachService;
import com.sport.service.web.models.coach.ListCoachResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/coaches")
@RequiredArgsConstructor
@Slf4j
public class CoachController {
    private final CoachService coachService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListCoachResponse getAllCoaches(
        @RequestParam(required = false) List<String> sportTypes,
        @RequestParam(required = false) Integer age,
        @RequestParam(required = false) String sex,
        @RequestParam(required = false) Integer yearsOfExperience
    ) {
        log.info("[API] GET /api/coaches | sportTypes={}, age={}, sex={}, yearsOfExperience={}",
                sportTypes, age, sex, yearsOfExperience);
        return coachService.findAllCoaches(sportTypes, age, sex, yearsOfExperience);
    }
}
