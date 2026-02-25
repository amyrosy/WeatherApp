package com.arjtech.weatherapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.arjtech.weatherapp.data.PlaceWeather
import com.arjtech.weatherapp.viewmodel.ForecastViewModel
import com.arjtech.weatherapp.model.ForecastResponse
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


private const val DEFAULT_ICON = "10n"

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel = viewModel()
) {

    val context = LocalContext.current
    var city by remember { mutableStateOf("") }

    // ✅ FIXED — use places instead of weatherList
    val weatherList by viewModel.places.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    /* ================= AUTO REFRESH ================= */

    LaunchedEffect(Unit) {
        viewModel.refreshAllPlaces()
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /* ================= HEADER ICON ================= */

    val headerIcon =
        weatherList.firstOrNull()?.icon ?: DEFAULT_ICON

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            /* ================= HEADER ================= */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {

                AsyncImage(
                    model = "https://openweathermap.org/img/wn/${headerIcon}@4x.png",
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.55f
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.01f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.01f)
                                )
                            )
                        )
                )

                Text(
                    text = "SkyCast",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            /* ================= CONTENT ================= */

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp, start = 3.dp, end = 3.dp),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp
                ),
                color = Color.White,
                tonalElevation = 0.dp
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {

                    /* ================= SEARCH ================= */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            placeholder = { Text("Search place") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {

                                val trimmedCity = city.trim()   // ✅ remove leading & trailing spaces

                                if (trimmedCity.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Please enter a valid place",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                viewModel.fetchForecast(trimmedCity)
                                city = ""
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Search")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LaunchedEffect(city) {
                        viewModel.searchPlaces(city)
                    }

                    if (city.isNotBlank() && searchResults.isNotEmpty()) {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF2F6FA)   // 🔵 background color
                            )
                        ) {

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {

                                itemsIndexed(searchResults) { index, place ->

                                    Column {

                                        Text(
                                            text = "${place.name}, ${place.state ?: ""}, ${place.country}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.fetchForecast(place.name)

                                                    Toast.makeText(
                                                        context,
                                                        "${place.name} added successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    city = ""
                                                }
                                                .padding(14.dp)
                                        )

                                        // 🔹 Divider line
                                        if (index != searchResults.lastIndex) {
                                            Divider(
                                                thickness = 0.6.dp,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    /* ================= EMPTY STATE ================= */

                    if (weatherList.isEmpty()) {

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "No places listed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Search a city to view weather details",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }

                    /* ================= WEATHER LIST ================= */

                    else {

                        LazyColumn {

                            items(weatherList) { place: PlaceWeather ->

                                var expanded by remember { mutableStateOf(false) }
                                var showDeleteDialog by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {

                                    Column(modifier = Modifier.padding(8.dp)) {

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(start=10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,

                                        ) {

                                            Text(
                                                place.cityName,
                                                style = MaterialTheme.typography.titleLarge
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("${place.temperature.toInt()}°C")
                                                Spacer(modifier = Modifier.width(8.dp))
                                                WeatherIcon(place.icon)
                                            }
                                        }

                                        Text(
                                            text = place.description,
                                            modifier = Modifier.padding(start=10.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(start=5.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            // DELETE ICON (LEFT SIDE)
                                            IconButton(
                                                onClick = {
                                                    showDeleteDialog = true
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete"
                                                )
                                            }

                                            // EXPAND ICON (RIGHT SIDE)
                                            IconButton(onClick = { expanded = !expanded }) {
                                                Icon(
                                                    if (expanded)
                                                        Icons.Default.KeyboardArrowUp
                                                    else
                                                        Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        if (expanded) {

                                            val forecastResponse = try {
                                                Gson().fromJson(place.forecastJson, ForecastResponse::class.java)
                                            } catch (e: Exception) {
                                                null
                                            }

                                            forecastResponse?.let { response ->

                                                val dailyForecasts =
                                                    response.list
                                                        .groupBy { it.dt_txt.substring(0, 10) }
                                                        .map { it.value.first() }
                                                        .take(5)

                                                Spacer(modifier = Modifier.height(8.dp))

                                                dailyForecasts.forEach { item ->

                                                    val inputFormat =
                                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                                                    val outputDay =
                                                        SimpleDateFormat("EEEE", Locale.getDefault())

                                                    val outputDate =
                                                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                                                    val date = inputFormat.parse(item.dt_txt)

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {

                                                        // LEFT SIDE → Day + Date
                                                        Column {
                                                            Text(
                                                                text = outputDay.format(date!!),
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Text(
                                                                text = outputDate.format(date),
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        }

                                                        // RIGHT SIDE → Temp + Icon
                                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                                            Text("${item.main.temp.toInt()}°C")

                                                            Spacer(modifier = Modifier.width(6.dp))

                                                            WeatherIcon(item.weather[0].icon)
                                                        }
                                                    }
                                                }
                                            }
                                        }


                                    }
                                }

                                if (showDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteDialog = false },
                                        confirmButton = {},
                                        dismissButton = {},
                                        text = {

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(0.dp) // 👈 reduce overall padding here
                                            ) {

                                                Text(
                                                    text = "Delete Place",
                                                    style = MaterialTheme.typography.titleLarge
                                                )

                                                Spacer(modifier = Modifier.height(6.dp)) // 👈 smaller gap

                                                Text(
                                                    text = "Are you sure you want to remove ${place.cityName}?"
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {

                                                    TextButton(onClick = { showDeleteDialog = false }) {
                                                        Text("No")
                                                    }

                                                    TextButton(
                                                        onClick = {
                                                            showDeleteDialog = false
                                                            viewModel.deletePlace(place)

                                                            Toast.makeText(
                                                                context,
                                                                "${place.cityName} removed successfully",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    ) {
                                                        Text("Yes")
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
