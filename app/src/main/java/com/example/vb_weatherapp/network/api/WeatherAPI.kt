package com.example.vb_weatherapp.network.api

import com.example.vb_weatherapp.data.OpenCageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    companion object {
        const val BASE_URL = "https://api.opencagedata.com/geocode/v1/"
        const val API_KEY = "f2b62a1d71064240adc17af323c7e1b4"
    }

    @GET("json")
    suspend fun searchLocation(
        @Query("key") key: String = API_KEY,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): Response<OpenCageResponse>
}