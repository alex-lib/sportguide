package com.sport.service.web.models.coach

data class CoachFilter(
    val sportTypes: List<String>?,
    val age: Int?,
    val sex: String?,
    val yearsOfExperience: Int?,
)