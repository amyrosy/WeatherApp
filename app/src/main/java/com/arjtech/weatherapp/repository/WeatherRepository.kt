package com.arjtech.weatherapp.repository

import android.content.Context
import com.arjtech.weatherapp.api.GeoRetrofitInstance
import com.arjtech.weatherapp.api.RetrofitInstance
import com.arjtech.weatherapp.data.AppDatabase
import com.arjtech.weatherapp.data.PlaceWeather
import com.arjtech.weatherapp.model.ForecastResponse
import com.arjtech.weatherapp.model.GeoResponse
import kotlinx.coroutines.flow.Flow

class WeatherRepository(context: Context) {   // This class takes Context as parameter Because Room needs context to build database- AppDatabase.getDatabase(context)

    private val dao = AppDatabase.getDatabase(context).placeWeatherDao()   // .getDatabase(context) → gets Singleton database
                                                                            // .placeWeatherDao() → gets DAO
                                                                            // dao now has: insert(), delete(), getAllPlaces()

    private val API_KEY = "d30038eafd4e1749d73acb1359c2e0c2"         // Used for calling OpenWeather API.

    suspend fun fetchForecast(city: String): ForecastResponse {    // ForecastResponse is a Data Transfer Object (DTO) used to map the JSON response from the OpenWeather API into a Kotlin object using Retrofit.
                                                                    // We CANNOT directly store ForecastResponse into Room because: It contains nested objects, It contains lists, It does not match your database schema, Room needs a proper Entity..
                                                                    // So we transform API model → Database model. And the process is called Data Mapping.
        return RetrofitInstance.api.getForecast(           // Calls Retrofit API interface -> Makes network request -> Returns ForecastResponse -> It is suspend- runs in coroutine
                                                            // Flow : Repository → Retrofit → Internet → API Server → JSON → ForecastResponse
            city = city,
            apiKey = API_KEY
        )
    }

    suspend fun insert(place: PlaceWeather) {     // This stores weather data locally.
                                                    // Flow: ViewModel → Repository → DAO → Room → SQLite
        dao.insert(place)
    }

    fun getAllPlaces(): Flow<List<PlaceWeather>> {   // This returns a Flow.  (Flow definition at the end_
        return dao.getAllPlaces()
    }

    suspend fun deletePlace(place: PlaceWeather) {   // Deletes a specific city from DB. Flow will automatically update UI.
        dao.deletePlace(place)
    }

    suspend fun searchPlaces(query: String): List<GeoResponse> {   // This is another network call.
                                                                    //        Purpose:
                                                                    //            User types city
                                                                    //            API returns matching cities
        return GeoRetrofitInstance.api.searchCity(
            city = query,
            apiKey = API_KEY
        )
    }
}

// Repository : the middle layer. (The Data Manager)
        // UI → ViewModel → Repository → (Room + Retrofit)
    // This repository : Talks to API (Retrofit), Talks to Database (Room), Decides where data comes from, Keeps UI clean.
    // Handles: Getting data from Internet , Saving data into Database, Getting data from Database and Giving data to ViewModel
    // In short : Repository abstracts data sources (local database and remote API) and provides a clean API to the ViewModel.



// Whats a Flow?

// Flow is a stream of data that:
    // Emits values when database changes
    // Automatically updates UI
// If:
    // You insert new city → Flow emits new list
    // You delete city → Flow emits updated list

//So UI automatically updates.
//This is reactive programming.


//❓ Why use Repository?
//        Ans: To separate data sources from ViewModel and follow clean architecture.

//❓ Why use Flow?
//        Ans: Flow allows reactive stream of data from Room. UI automatically updates when database changes.

//❓ Why suspend functions?
//        Ans: Network and database operations are long-running tasks. Suspend functions allow them to run asynchronously without blocking the main thread.

//❓ What design pattern is this?
//        Ans: MVVM with Repository pattern.

//❓ Why convert API model to Entity?
//        Ans: API models are designed for network responses and may contain nested structures and unnecessary fields.
//              Entities are designed for local storage. So we map API models to entities before storing in Room.
//              Entity means a class that represent the database table.
//
//❓ Why store forecastJson as String?
//        Ans: Room cannot directly store complex nested objects or lists. So we serialize them into JSON string for storage.


//                    User types city
//                            ↓
//                    ViewModel calls repository.fetchForecast()
//                            ↓
//                    Retrofit calls API
//                            ↓
//                    Receives ForecastResponse
//                            ↓
//                    Convert to PlaceWeather
//                            ↓
//                    repository.insert(place)
//                            ↓
//                    Room stores in DB
//                            ↓
//                    Flow emits updated list
//                            ↓
//                    UI updates automatically


// What is an API?
    // An API is a bridge that allows two software systems to communicate with each other.
        // Waiter goes to kitchen → brings response → gives to you. ..That waiter is the API layer.

// API in our app : OpenWeather API
 // When you do: RetrofitInstance.api.getForecast(city, apiKey)
            // our app will : Sends a request to OpenWeather server -> Server processes it -> Server sends back JSON data -> Retrofit converts JSON to Kotlin object