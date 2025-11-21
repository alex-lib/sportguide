package com.sport.service.web.models.place

import jakarta.validation.constraints.NotBlank

data class PlaceFilter(
    @field:NotBlank(message = "District must be pointed")
    val district: String,
    @field:NotBlank(message = "Sub district must be pointed")
    val subDistrict: String,
    val outdoor: String?,
    val placeType: String?
)