package com.arjtech.weatherapp.model

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)
