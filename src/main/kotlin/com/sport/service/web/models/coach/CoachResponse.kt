package com.sport.service.web.models.coach

class CoachResponse(
    val name: String,
    val sportTypes: List<String>?,
    val description: String?,
    val age: Int?,
    val sex: String?,
    val yearsOfExperience: Int?,
    val education: String?,
    val phoneNumber: String?,
    val workPlacesNames: List<String>?,
    val photo: ByteArray?,
    val telegramUsername: String?,
    val trainingProgramsNames: List<String>?
)