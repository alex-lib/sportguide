package com.sport.service.web.models.joint_training

import com.sport.service.validation.joint_training.PhoneNumberValid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateJointTrainingRequest(
    @field:Size(
        min = 5,
        max = 35,
        message = "Title must contains between {min} and {max} characters"
    )
    val title: String,
    @field:Size(
        min = 10,
        max = 100,
        message = "Description must contains between {min} and {max} characters"
    )
    val description: String,
    val date: String,
    val time: String?,
    @field:NotBlank(message = "Sport type must be pointed")
    val sportType: String,
    val placeName: String?,
    @field:NotBlank(message = "District must be pointed")
    val district: String?,
    val address: String?,
    @field:PhoneNumberValid
    val phoneNumber: String?,
    @field:Size(
        min = 2,
        max = 35,
        message = "Name must be pointed and contains between {min} and {max} characters"
    )
    val creatorName: String
)