package com.sport.service.web.models.coach

import com.sport.service.entities.enums.common.SportType
import jakarta.validation.constraints.NotBlank

data class CreateCoachRequest(
    @field:NotBlank(message = "Coach's id must be pointed")
    val subscriberId: Long,
    @field:NotBlank(message = "Name must be pointed")
    val name: String,
    val sportTypes: List<SportType>,
    @field:NotBlank(message = "Description must be pointed")
    val description: String,
    val age: Int?,
    @field:NotBlank(message = "Sex must be pointed")
    val sex: String,
    val yearsOfExperience: Int,
    val education: String,
    val phoneNumber: String,
    val workPlacesNames: List<String>,
    @field:NotBlank(message = "Photo must be uploaded")
    val photo: ByteArray,
    val monthsForSubscriptionToBeCoach: Short,
    val showInWeb: Boolean,
    val trainingProgramsTitles: List<String>?
)
