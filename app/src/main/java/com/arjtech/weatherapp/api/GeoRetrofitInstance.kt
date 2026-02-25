package com.arjtech.weatherapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeoRetrofitInstance {

    private const val BASE_URL =
        "https://api.openweathermap.org/geo/1.0/"

    val api: GeoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoApi::class.java)
    }
}