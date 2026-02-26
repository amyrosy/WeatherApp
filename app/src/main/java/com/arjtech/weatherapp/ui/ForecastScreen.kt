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

@Composable             // function that creates UI
fun ForecastScreen(     // our weather screen
    viewModel: ForecastViewModel = viewModel()      // this gets the viewModel automatically
) {

    val context = LocalContext.current              // gives the android context . (Without this context, the toast cannot show)
    var city by remember { mutableStateOf("") }     // remember -> will keep the value during recomposition.
                                                            // mutableStateOf("") -> Default empty text
    // ✅ FIXED — use places instead of weatherList
    // collectAsState() simply means -> It Listen to this Flow and update the UI automatically when data changes.
    // without collectAsState() -> UI will NOT update when Flow changes.
    val weatherList by viewModel.places.collectAsState()        // viewModel gives the StateFlow.   collectAsState() is used because Compose UI cannot directly observe Flow.
    val searchResults by viewModel.searchResults.collectAsState()

    /* ================= AUTO REFRESH ================= */

    LaunchedEffect(Unit) {      // LaunchedEffect(Unit) -> runs once when the screen loads.
        viewModel.refreshAllPlaces()    // when the screen opens, it will refresh all the cities automatically.
    }

    LaunchedEffect(Unit) {          // shows the error message . If the viewModel emits "Places Not Found" then show this toast message
        viewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /* ================= HEADER ICON ================= */

    val headerIcon =
        weatherList.firstOrNull()?.icon ?: DEFAULT_ICON         // If the list has data, then it will show the first city icon or will show default icon.


    // Structure : Box - > Column -> Header -> Content

    // a Box is a layout container that holds UI elements and places them on top of each other. Think of a Box like a container or frame 📦 where you can put things inside.
    // We use this Box when we want Overlapping Items: like image as background, text on top of image.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            /* ================= HEADER  portion with the icon and text================= */
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                //the image
                AsyncImage(                 // Loads image from internet. Used to load the weather icon.
                    model = "https://openweathermap.org/img/wn/${headerIcon}@4x.png",   //headerIcon is the icon of each weather ... @4x,@2x etc will control the image size/quality.. @2x -> medium size and @4x -> large high quality icon
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.55f
                )
                // the overlay given to that image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(        // Brush.verticalGradient → color goes top → bottom
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.01f),     // we are giving this twice because gradient needs to colour (at the start and end) .. here we are giving the same colour for both
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.01f)
                                )
                            )
                        )
                )
                // the text
                Text(
                    text = "SkyCast",                   // Typography = text styling system (font size, weight, style).
                    style = MaterialTheme.typography.headlineLarge.copy(        // headlineLarge → predefined big font style , .copy() → modify it if needed
                                                                    // headlineLarge already have default size, font weight, style ec . but if we want to modify only something from this, we will give within .copy. So it will take the original style and modify only these values.
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,                       // give spacing between letters like S K Y C A S T
                        color = MaterialTheme.colorScheme.onPrimaryContainer    // set text colour
                    )
                )
            }

            /* ================= CONTENT ================= */
            //Surface is a UI container that holds content.
            Surface(                    //  applies background colour, elevation, shape, Follow material design theme
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp, start = 3.dp, end = 3.dp),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp),
                color = Color.White,
                tonalElevation = 0.dp       // Elevation = shadow depth
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

                        OutlinedTextField(      // when the user types -> city will be updated
                            value = city,
                            onValueChange = { city = it },
                            placeholder = { Text("Search place") },
                            singleLine = true,          // user can type only one line (no multiple line text)
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {

                                val trimmedCity = city.trim()   // Removes the space at the beginning and end

                                if (trimmedCity.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Please enter a valid place",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button           // Stop execution of button click. Do not run the remaining code.
                                }

                                viewModel.fetchForecast(trimmedCity)        // calling function within the viewModel to fetch the weather from API
                                city = ""
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Search")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LaunchedEffect(city) {          // when city changes -> call GEO api -> show suggestions
                        viewModel.searchPlaces(city)
                    }

                    if (city.isNotBlank() && searchResults.isNotEmpty()) {

                        // we use Card to group related informations like weather details, user profile, product item etc.. This will separate content from background bcs it has elevation, rounded corners etc.
                                    // You can use Box, but:
                                        // Box → just layout
                                        // Card → layout + Material styling + elevation + shape
                        //Card is easier and cleaner for UI design
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(6.dp),       // Card shadow depth = 6dp
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF2F6FA)                      // 🔵 Card background color
                            )
                        ) {
                            // to list cities in the search
                            LazyColumn(             // A scrollable vertical list that loads items only when needed.
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {
                                    // itemsIndexed is Used inside LazyColumn.
                                itemsIndexed(searchResults) { index, place ->       // loop throughout the list and gives the index (position number)

                                    Column {
                                        Text(
                                            text = "${place.name}, ${place.state ?: ""}, ${place.country}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.fetchForecast(place.name)     // city is added

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
                                        if (index != searchResults.lastIndex) {             // lastIndex means the last position in the list . To avoid the divider after the last index.
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
                        // to list the contents
                        LazyColumn {

                            items(weatherList) { place: PlaceWeather ->         // items -> Used inside LazyColumn to display list items.

                                var expanded by remember { mutableStateOf(false) }          // shows the arrow button to list the weather of next 5 days
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
                                            IconButton(
                                                onClick = { expanded = !expanded }
                                            ) {
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
                                            val forecastResponse = try {                                                        // Gson is a converter between JSON and Kotlin objects.
                                                Gson().fromJson(place.forecastJson, ForecastResponse::class.java)       // we stored the Api responses as the Json String. Now Converting the Json back to the kotlin object.
                                            } catch (e: Exception) {
                                                null
                                            }

                                            forecastResponse?.let { response ->                 // .let means is the forecastResponse is not null, then execute this block.(to avoid crash)

                                                val dailyForecasts =
                                                    response.list               // substring(0, 10) means we are taking the 1st 10 substring from the date ( which is in form of date and time) to get only the date like 2026-02-26 from that whole date
                                                            .groupBy { it.dt_txt.substring(0, 10) }      // group by date because the api gives every 3hr forecast like 2026-02-26 09:00, 2026-02-26 12:00, 2026-02-26 15:00 . So we group them by 2026-02-26.
                                                            .map { it.value.first() }                    // takes the first forecast per day.   So instead of 8 forecasts per day, we take only one.
                                                            .take(5)                                // take 5 days only

                                                Spacer(modifier = Modifier.height(8.dp))

                                                dailyForecasts.forEach { item ->

                                                    val inputFormat =                                      // This matches API date format like 2026-02-26 12:00:00
                                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                                                    val outputDay =                                         // Shows Monday, Tuesday, etc.
                                                        SimpleDateFormat("EEEE", Locale.getDefault())

                                                    val outputDate =                                        // Shows 26.02.2026
                                                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                                                    val date = inputFormat.parse(item.dt_txt)       // Convert string → Date object.

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


                                if (showDeleteDialog) {         // when delete icon is clicked, the AlertDialog appears
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

                                                    TextButton(
                                                        onClick = { showDeleteDialog = false }
                                                    ) {
                                                        Text("No")
                                                    }

                                                    TextButton(
                                                        onClick = {
                                                            showDeleteDialog = false
                                                            viewModel.deletePlace(place)        // delete from database

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

//    Flow → live data stream
//    collectAsState() → connects Flow to UI
//    Compose re-runs → UI refresh
//    Gson → convert JSON to object
//    groupBy → group by date
//    map → transform list
//    take(5) → limit list
//    trim() → remove extra spaces
//    return@Button → stop button execution
//    typography → text styling system
//    dp → size unit
//    sp → text size unit

// UI items explanation

//      Modifier -> Used to style UI. For padding, fillMaxSize, background, clickable etc.
//      Spacer   -> Adds empty space between components.

//      Recomposition -> Compose re-runs that composable function.
//      “Compose re-runs that composable function” means in jetpack compose, UI is just functions(@composable) .
//                      So when the data changes -> compose calls that function again. (which is called recomposition)

// Diff between stateFlow and SharedFlow
//          StateFlow -> will keep the last value. Toast can be shown again.
//          SharedFlow -> will not keep any last value. Toast will be shown only once and will pass to another activity.

//    | Concept        | Why Used                      |
//    | -------------- | ----------------------------- |
//    | StateFlow      | Auto UI update                |
//    | collectAsState | Convert Flow to Compose state |
//    | remember       | Preserve state                |
//    | mutableStateOf | Track changes                 |
//    | LaunchedEffect | Run side-effects              |
//    | LazyColumn     | Efficient list                |
//    | Gson           | Convert JSON ↔ Object         |
//    | AlertDialog    | Confirmation UI               |



//            User types city
//                  ↓
//            ViewModel.searchPlaces()
//                  ↓
//            Geo API
//                  ↓
//            UI shows suggestions
//                  ↓
//            User selects
//                  ↓
//            ViewModel.fetchForecast()
//                  ↓
//            API call
//                  ↓
//            Save in Room
//                  ↓
//            UI updates automatically
