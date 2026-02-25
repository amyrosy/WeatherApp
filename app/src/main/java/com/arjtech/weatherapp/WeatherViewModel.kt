package com.arjtech.weatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjtech.weatherapp.data.PlaceWeather
import com.arjtech.weatherapp.data.PlaceWeatherDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val dao: PlaceWeatherDao
) : ViewModel() {

    val places: StateFlow<List<PlaceWeather>> =
        dao.getAllPlaces()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun insert(place: PlaceWeather) {
        viewModelScope.launch {
            dao.insert(place)
        }
    }
}
