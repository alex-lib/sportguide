package com.sport.service.web.controllers;

import com.sport.service.services.JointTrainingService;
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest;
import com.sport.service.web.models.joint_training.JointTrainingFilter;
import com.sport.service.web.models.joint_training.ListJointTrainingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/joint-trainings")
@RequiredArgsConstructor
public class JointTrainingController {

    private final JointTrainingService jointTrainingService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListJointTrainingResponse getAllJointTrainings(JointTrainingFilter filter) {
        return jointTrainingService.findAllJointTrainings(filter);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @PostMapping
    public void createJointTraining(@Valid @RequestBody CreateJointTrainingRequest request,
                                    @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = jwt.getClaim("id");
        jointTrainingService.createJointTraining(request, userId);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @PutMapping("/{id}")
    public void updateJointTraining(@Valid @RequestBody CreateJointTrainingRequest request,
                                    @PathVariable Long id,
                                    @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = jwt.getClaim("id");
        jointTrainingService.updateJointTrainingById(request, id, userId);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteJointTraining(@PathVariable Long id,
                                    @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = jwt.getClaim("id");
        jointTrainingService.deleteJointTrainingById(id, userId);
    }
}