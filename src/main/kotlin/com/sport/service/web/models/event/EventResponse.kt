package com.sport.service.web.models.event

data class EventResponse(
    val name: String?,
    val description: String?,
    val placeName: String?,
    val district: String,
    val address: String,
    val link: String?,
    val date: String,
    val time: String?,
    val coordinates: String?
)