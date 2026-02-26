package com.arjtech.weatherapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao            // Data Access Object
interface PlaceWeatherDao {

    @Query("SELECT * FROM places_weather ORDER BY lastUpdated DESC")
    fun getAllPlaces(): Flow<List<PlaceWeather>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)  // This is used to insert a new row into the database table places_weather.
                                                // onConflict = OnConflictStrategy.REPLACE means ->  If a row with the same primary key already exists, it will replace the old row with the new one.
                                                // In our case, the primary key is cityName, so if you insert a PlaceWeather with a city that already exists, it updates the record.
    suspend fun insert(placeWeather: PlaceWeather)   // suspend indicates this function is a Kotlin coroutine function, so it runs asynchronously without blocking the main thread.

    @Query("DELETE FROM places_weather")
    suspend fun deleteAll()

    @Delete
    suspend fun deletePlace(place: PlaceWeather)
}

// Insert and delete are suspend fns because it does not run on the main thread . Instead it must run in the background.
