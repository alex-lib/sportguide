package com.sport.service.web.controllers;

import com.sport.service.services.TrainingProgramService;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import com.sport.service.web.models.training_program.TrainingProgramFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("api/training-programs")
@RequiredArgsConstructor
public class TrainingProgramController {
    private final TrainingProgramService trainingProgramService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListTrainingProgramResponse getAllTrainingPrograms(TrainingProgramFilter filter) {
        return trainingProgramService.findAll(filter);
    }
}