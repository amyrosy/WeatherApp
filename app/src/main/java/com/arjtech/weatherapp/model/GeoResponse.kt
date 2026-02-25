package com.arjtech.weatherapp.model

data class GeoResponse(
    val name: String,
    val state: String?,
    val country: String,
    val lat: Double,
    val lon: Double
)