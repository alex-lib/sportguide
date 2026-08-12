package com.sport.service.web.controllers;

import com.sport.service.services.CoachService;
import com.sport.service.services.TrainingProgramService;
import com.sport.service.web.models.coach.CoachRequest;
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final CoachService coachService;
    private final TrainingProgramService trainingProgramService;

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/coaches")
    public void createCoach(@RequestPart("data") @Valid CoachRequest request,
                            @RequestPart("photo") MultipartFile photo
    ) {
        coachService.createCoach(request, photo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/coaches/{id}")
    public void updateCoach(
            @PathVariable Long id,
            @RequestPart("data") @Valid CoachRequest request,
            @RequestPart("photo") MultipartFile photo
    ) {
        coachService.updateCoachById(id, request, photo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/coaches/{id}")
    public void deleteCoach(@PathVariable Long id) {
        coachService.deleteCoachById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/program-trainings")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTrainingProgram(
            @Valid @RequestBody CreateTrainingProgramRequest request
    ) {
        trainingProgramService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/program-trainings/{id}")
    public void updateTrainingProgram(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingProgramRequest request
    ) {
        trainingProgramService.update(request, id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/program-trainings/{id}")
    public void deleteTrainingProgram(@PathVariable Long id) {
        trainingProgramService.delete(id);
    }
}