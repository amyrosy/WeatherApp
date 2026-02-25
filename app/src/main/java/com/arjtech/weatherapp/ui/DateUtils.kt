package com.arjtech.weatherapp.ui

import java.text.SimpleDateFormat
import java.util.*

fun formatDate(dateTime: String): String {
    val input =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val output =
        SimpleDateFormat("EEEE dd.MM.yyyy", Locale.getDefault())

    val date = input.parse(dateTime)
    return output.format(date!!)
}
