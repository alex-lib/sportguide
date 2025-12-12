package com.sport.service.web.models.place

data class PlaceResponse(
    val name: String,
    val district: String,
    val subDistrict: String?,
    val address: String,
    val description: String?,
    val webSite: String?,
    val outdoor: String,
    val placeType: String,
    val photo: ByteArray,
    val coordinates: String?,
)