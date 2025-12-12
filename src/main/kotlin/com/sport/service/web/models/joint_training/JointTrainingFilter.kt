package com.sport.service.web.models.joint_training

data class JointTrainingFilter(
    val date: String?,
    val sportType: List<String>?,
    val district: String?
)