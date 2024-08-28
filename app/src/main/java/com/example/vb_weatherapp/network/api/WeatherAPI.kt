package com.example.vb_weatherapp.network.api

import com.example.vb_weatherapp.data.RemoteLocation
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    companion object {
        const val BASE_URL = "https://openweathermap.org/api/one-call-api"
        const val API_KEY = "8ddadecc7ae4f56fee73b2b405a63659"
    }

    @GET("search.json")
    suspend fun searchLocation(
        @Query("key") key: String = API_KEY,
        @Query("q") query: String
    ): Response<List<RemoteLocation>>

}