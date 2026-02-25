package com.arjtech.weatherapp.ui

import androidx.compose.runtime.Composable
import coil.compose.AsyncImage

@Composable
fun WeatherIcon(iconCode: String) {
    AsyncImage(
        model = "https://openweathermap.org/img/wn/${iconCode}@2x.png",
        contentDescription = null
    )
}

