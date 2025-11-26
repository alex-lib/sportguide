package com.sport.service.web.models.joint_training

import java.time.LocalDate
import java.time.LocalTime

data class JointTrainingResponse(
    val title: String,
    val description: String,
    val date: LocalDate,
    val time: LocalTime,
    val sportType: String,
    val placeName: String,
    val district: String,
    val address: String,
    val creatorName: String,
    val phoneNumber: String,
    val linkToChatWithCreator: String
)