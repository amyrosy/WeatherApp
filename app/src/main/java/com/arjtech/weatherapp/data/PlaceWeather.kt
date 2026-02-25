package com.arjtech.weatherapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places_weather")  // table name inside the database.

data class PlaceWeather(

    @PrimaryKey
    val cityName: String,  // cityName is a primary key.. so duplicate city name will not be allowed. If entered, it will be replaced with the lastest values.
    val temperature: Double,
    val description: String,
    val icon: String,
    val date: String,
    val forecastJson: String,
    val lastUpdated: Long
)


// "weather_database" = The whole database file. It can contain multiple tables. It is created only once
// "places_weather"   = It is a table inside the database.

// ❓ What is the difference between table name and database name in Room?
        //Ans: The database name represents the physical SQLite database file stored in the device.
                // The table name represents a specific data structure inside the database where rows are stored.
                // One database can contain multiple tables, and Room maps each Entity class to a table inside the database.
