package com.sport.service.repositories

import com.sport.service.entities.training_program.TrainingProgram
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TrainingProgramRepository : JpaRepository<TrainingProgram, Long> {

    fun findByTitle(title: String): Optional<TrainingProgram>
}