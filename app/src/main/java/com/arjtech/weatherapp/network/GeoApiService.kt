package com.arjtech.weatherapp.network

import com.arjtech.weatherapp.model.GeoPlace
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApiService {

    @GET("geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("appid") apiKey: String
    ): List<GeoPlace>
}