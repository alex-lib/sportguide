package com.sport.service.web.controllers;

import com.sport.service.services.CoachService;
import com.sport.service.web.models.coach.CoachFilter;
import com.sport.service.web.models.coach.ListCoachResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/coaches")
@RequiredArgsConstructor
public class CoachController {
    private final CoachService coachService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListCoachResponse getAllCoaches(CoachFilter filter) {
        return coachService.findAllCoaches(filter);
    }
}