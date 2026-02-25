package com.arjtech.weatherapp.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arjtech.weatherapp.data.PlaceWeather
import com.arjtech.weatherapp.model.ForecastResponse
import com.arjtech.weatherapp.model.GeoResponse
import com.arjtech.weatherapp.repository.WeatherRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// This code controls : Getting data from API , Saving data into Room database, Searching places, Updating UI using StateFlow, Handling errors

class ForecastViewModel(application: Application) :
    AndroidViewModel(application) {         // ViewModel = Manager between UI and Data

    private val repository = WeatherRepository(application)   // ViewModel does NOT directly talk to API or Database.
                                                                        // It asks: “Hey Repository, please give me data.”
                                                                                // Repository decides: Should I fetch from API?  or  Should I read from Room?

    /* ================= EXISTING DATABASE FLOW ================= */

    val places: StateFlow<List<PlaceWeather>> =    // It listens to database changes. It always holds the latest value.
        repository.getAllPlaces()                   // getAllPlaces() returns a Flow<List<PlaceWeather>>
            .stateIn(                               // we convert Flow → StateFlow using stateIn(). (Flow = Stream of data (like live updates). But UI needs a StateFlow)
                viewModelScope,                     // viewModelScope mean This flow will live as long as ViewModel lives.
                SharingStarted.WhileSubscribed(5000),   // Start collecting only when UI is observing it (when that screen is on). and Stop collecting 5 seconds after UI stops observing (it will wait for 5 sec even after the screen is off).  Why? To save resources.
                emptyList()             // Initial value before database returns data. So if database hasn’t responded yet, UI sees empty list.
            )

    /* ================= GEO SEARCH STATE ================= */

    private val _searchResults =                                    // This stores search results from Geo API.
        MutableStateFlow<List<GeoResponse>>(emptyList())   // MutableStateFlow = can change value.
                                                                    // But you don’t want UI to change it.

            //  _searchResults → private (editable) and  searchResults → public (read-only)
            // asStateFlow() makes it read-only.
    val searchResults: StateFlow<List<GeoResponse>> =         // _searchResults → internal and searchResults → public (UI reads this) .
        _searchResults.asStateFlow()                                //  Why this two? Ans : To protect data (encapsulation).

    private var currentLat: Double? = null
    private var currentLon: Double? = null

    init {
        refreshAllPlaces()
    }

    /* ================= LOCATION ================= */

    fun setUserLocation(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {

        val result = FloatArray(1)

        Location.distanceBetween(
            lat1, lon1,
            lat2, lon2,
            result
        )

        return result[0]
    }

    /* ================= GEO SEARCH ================= */

    // This function runs when user types city name.
    fun searchPlaces(query: String) {

        if (query.isBlank()) {      // if user types nothing then it will clear the search result and stop function.
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {             // Launch coroutine inside ViewModel. because the API call will take time and we cant block the main thread.

            try {
                val result = repository.searchPlaces(query)             // Call Geo API using repository.
                val lowerQuery = query.lowercase()

                val filtered = result.filter {
                    it.name.lowercase().startsWith(lowerQuery)
                }

                val sorted = filtered.sortedWith(           // sort cities by: Name match priority and Distance from user
                    compareBy<GeoResponse> {
                        !it.name.lowercase().startsWith(lowerQuery)     //Cities starting exactly with query come first.
                    }.thenBy {
                        if (currentLat != null && currentLon != null) {         // If user location exists: Sort by nearest distance., If not: Push to end
                            calculateDistance(
                                currentLat!!,
                                currentLon!!,
                                it.lat,
                                it.lon
                            )
                        } else Float.MAX_VALUE
                    }
                )

                _searchResults.value = sorted.take(15)          // Show only top 15 results.

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* ================= EXISTING FORECAST ================= */

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    fun fetchForecast(city: String) {       // Runs when user selects a city.
        viewModelScope.launch {             // Launch coroutine.
            try {
                val response = repository.fetchForecast(city)   // Call Forecast API.
                saveToDatabase(response)                        // Convert API model → Entity → Save in Room.
            } catch (e: Exception) {
                _errorMessage.emit("Place not found")
            }
        }
    }

    private fun saveToDatabase(response: ForecastResponse) {

        val current = response.list.firstOrNull() ?: return         // Take first forecast item. If empty → stop.

        val place = PlaceWeather(               // This is API → Entity mapping.   Why because - API model is complex and Room needs simple flat object
            cityName = response.city.name,
            temperature = current.main.temp,
            description = current.weather[0].description,
            icon = current.weather[0].icon,
            date = current.dt_txt,
            forecastJson = Gson().toJson(response),     // Room cannot store full complex object. So we convert entire API response into JSON String and we store it and then Later we can convert it back.
            lastUpdated = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insert(place)
        }
    }

    // Auto refresh saved cities.
    fun refreshAllPlaces() {
        viewModelScope.launch {
            places.value.forEach { place ->         // Loop through all saved cities.
                try {
                    val updatedResponse =           // Fetch fresh data for each city.
                        repository.fetchForecast(place.cityName)

                    saveToDatabase(updatedResponse)     // Update database.

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deletePlace(place: PlaceWeather) {
        viewModelScope.launch {
            repository.deletePlace(place)
        }
    }
}


//    ViewModel = overall manager
//    Repository = data manager
//    API + Room = data sources

// Data Source = From where data comes
// we have 2 data sources :
    // 1. API → Internet data (OpenWeather)
    // 2. Room → Local database (phone storage)


//    Coroutine → A background worker that does tasks without freezing the app.
//                          A coroutine is a lightweight background task in Kotlin. It lets your app do long work like calling an API or reading a database without freezing the screen. Instead of blocking the main thread (UI thread), the coroutine runs the work in the background and keeps the app smooth.

//    StateFlow → A data holder that always has a value and updates the UI when it changes.
//                          StateFlow is a data holder that always contains a current value and automatically updates the UI when the value changes. It is used inside the ViewModel to send data to the UI. When the data changes (like weather list updates), the UI immediately receives the new value.

//    SharingStarted → A rule that decides when a Flow should start or stop working.
//                          SharingStarted decides when a Flow should start and stop working. It controls when the data stream becomes active. This helps save memory and system resources by not running unnecessary work.

//    WhileSubscribed(5000) → Work only while the UI is watching, and stop 5 seconds after it’s not.
//                          WhileSubscribed(5000) means the Flow will stay active only when the UI is observing it. If the UI stops observing (like when the screen is closed), it waits 5 seconds before stopping. This prevents restarting the flow too often during quick screen changes like rotation.

// Difference between Flow and StateFlow  (Both will update the UI)
// Flow      :  Flow is a stream of data that sends values one by one when someone collects it. It does not store the last value. If nobody collects it → it does nothing. (It is cold.)
//              Flow will NOT start producing data unless you call: collect { } . This is called collecting the flow.
//              It works only when someone asks for data. and its lazy .
// StateFlow :  StateFlow is a special type of Flow that -> Always has a current value , Remembers the latest value, Immediately gives the latest value to new observers, Is hot (always active once created)
//          Main Diff is : StateFlow always has a current value ready but Flow does not.

    // Room gives us a Flow. But UI needs current value immediately, automatic updates, A value even before the database responds.
    // So we convert Flow to StateFlow using stateIn()

// Example :
//      Flow      : If UI starts collecting late- It will NOT get previous value. It only gets new emissions.
//      StateFlow : Even if UI starts observing late- It immediately receives the latest stored value. Because it stores state.

// Real-Life Example:
//      Flow      : Live Cricket Commentary. If u start listening late, then we cant know the current score, we can only listen to the next score.
//      StateFlow : Cricket Scoreboard . Even if u are late, u can see the current score also on the scoreboard.


//❓ Why use filter and sortedWith?
//  ✔ To improve user experience.

//❓ Why use coroutine?
//  ✔ To avoid blocking UI.

//❓ Why use SharedFlow for errors?
//  ✔ Because errors are one-time events (Toast).

//❓ Why not store ForecastResponse directly?
//  ✔ Room cannot store complex nested structures.

//❓ What is mapping?
//  ✔ Converting API model → Entity.