package com.arjtech.weatherapp.api

import com.arjtech.weatherapp.model.GeoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApi {

    @GET("direct")
    suspend fun searchCity(
        @Query("q") city: String,
        @Query("limit") limit: Int = 50,
        @Query("appid") apiKey: String
    ): List<GeoResponse>
}