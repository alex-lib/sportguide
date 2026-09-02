package com.sport.service.web.controllers;

import com.sport.service.services.TrainingProgramService;
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTrainingProgram(
            @Valid @RequestBody CreateTrainingProgramRequest request
    ) {
        trainingProgramService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public void updateTrainingProgram(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingProgramRequest request
    ) {
        trainingProgramService.update(request, id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteTrainingProgram(@PathVariable Long id) {
        trainingProgramService.delete(id);
    }
}