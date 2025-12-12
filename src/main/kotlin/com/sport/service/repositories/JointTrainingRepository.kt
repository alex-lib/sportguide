package com.sport.service.repositories

import com.sport.service.entities.JointTraining
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

import org.springframework.stereotype.Repository

@Repository
interface JointTrainingRepository : JpaRepository<JointTraining, Long>, JpaSpecificationExecutor<JointTraining>