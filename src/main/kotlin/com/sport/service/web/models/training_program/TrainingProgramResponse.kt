package com.sport.service.web.models.training_program

import java.math.BigDecimal

data class TrainingProgramResponse(
    val title: String,
    val creators: List<String>,
    val price: BigDecimal,
    val description: String,
    val sportTypes: List<String>
)