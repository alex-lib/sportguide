package com.sport.service.services.impl

import com.sport.service.entities.training_program.TrainingProgram
import com.sport.service.repositories.TrainingProgramRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrainingProgramServiceImpl(
    private var trainingProgramRepository: TrainingProgramRepository,
) {

    fun findByTitle(title: String): Optional<TrainingProgram> {
        return trainingProgramRepository.findByTitle(title)
    }
}