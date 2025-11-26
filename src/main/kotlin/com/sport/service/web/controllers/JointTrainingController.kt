package com.sport.service.web.controllers

import com.sport.service.services.impl.JointTrainingServiceImpl
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest
import com.sport.service.web.models.joint_training.JointTrainingFilter
import com.sport.service.web.models.joint_training.ListJointTrainingResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/joint-trainings")
class JointTrainingController(
    private var jointTrainingServiceImpl: JointTrainingServiceImpl
) {

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    fun findAll(filter: JointTrainingFilter): ListJointTrainingResponse {
        return jointTrainingServiceImpl.findAll(filter)
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody request: CreateJointTrainingRequest, jwt: JwtAuthenticationToken) {
        return jointTrainingServiceImpl.create(request, jwt)
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @PutMapping("/{id}")
    fun update(
        @Valid @RequestBody request: CreateJointTrainingRequest,
        @PathVariable id: Long,
        jwt: JwtAuthenticationToken
    ) {
        return jointTrainingServiceImpl.update(request, id, jwt)
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, jwt: JwtAuthenticationToken) {
        return jointTrainingServiceImpl.delete(id, jwt)
    }
}