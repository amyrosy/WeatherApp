package com.arjtech.weatherapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arjtech.weatherapp.model.ForecastItem
@Composable
fun ForecastDayItem(item: ForecastItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(formatDate(item.dt_txt))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${item.main.temp.toInt()}°C")
            Spacer(modifier = Modifier.width(8.dp))
            WeatherIcon(item.weather[0].icon)
        }
    }
}

