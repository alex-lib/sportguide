package com.sport.service.web.models.training_program

import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class CreateTrainingProgramRequest(
    @field:NotBlank(message = "Coach's/es' name/s must be pointed")
    val coachesId: List<Long>,
    @field:NotBlank(message = "Title must be pointed")
    val title: String,
    val price: BigDecimal,
    val description: String,
    val sportTypes: List<String>,
    val showInWeb: Boolean
)