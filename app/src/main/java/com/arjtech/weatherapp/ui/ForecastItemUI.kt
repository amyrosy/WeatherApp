package com.arjtech.weatherapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arjtech.weatherapp.model.ForecastItem
@Composable
fun ForecastItemUI(item: ForecastItem) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.dt_txt.substring(0, 10)) // Date
            Text("${item.main.temp}°C")
            Text(item.weather[0].description)
        }
    }
}
