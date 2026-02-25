package com.arjtech.weatherapp.ui

fun getWeatherIconUrl(icon: String): String {
    return "https://openweathermap.org/img/wn/${icon}@4x.png"
}
