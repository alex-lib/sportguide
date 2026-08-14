package com.sport.service.web.controllers;

import com.sport.service.services.TrainingProgramService;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/training-programs")
@RequiredArgsConstructor
@Slf4j
public class TrainingProgramController {
    private final TrainingProgramService trainingProgramService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListTrainingProgramResponse getAllTrainingPrograms(
        @RequestParam(required = false) List<String> sportTypes
    ) {
        log.info("[API] GET /api/training-programs | sportTypes={}", sportTypes);
        return trainingProgramService.findAll(sportTypes);
    }
}