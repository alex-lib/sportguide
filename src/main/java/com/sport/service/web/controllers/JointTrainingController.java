package com.sport.service.web.controllers;

import com.sport.service.services.JointTrainingService;
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest;
import com.sport.service.web.models.joint_training.ListJointTrainingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("api/joint-trainings")
@RequiredArgsConstructor
@Slf4j
public class JointTrainingController {

    private final JointTrainingService jointTrainingService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListJointTrainingResponse getAllJointTrainings(
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String date,
        @RequestParam(required = false) List<String> sportType,
        @RequestParam(required = false) String search
    ) {
        log.info("[API] GET /api/joint-trainings | district={}, date={}, sportType={}, search={}",
                district, date, sportType, search);
        return jointTrainingService.findAllJointTrainings(district, date, sportType, search);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @PostMapping
    public void createJointTraining(@Valid @RequestBody CreateJointTrainingRequest request,
                                     Authentication auth
    ) {
        Long userId = ((Long) auth.getPrincipal());
        jointTrainingService.createJointTraining(request, userId);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @PutMapping("/{id}")
    public void updateJointTraining(@Valid @RequestBody CreateJointTrainingRequest request,
                                     @PathVariable Long id,
                                     Authentication auth
    ) {
        Long userId = ((Long) auth.getPrincipal());
        jointTrainingService.updateJointTrainingById(request, id, userId);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteJointTraining(@PathVariable Long id,
                                    Authentication auth
    ) {
        Long userId = ((Long) auth.getPrincipal());
        jointTrainingService.deleteJointTrainingById(id, userId);
    }
}