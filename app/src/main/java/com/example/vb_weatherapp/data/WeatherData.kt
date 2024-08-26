package com.example.vb_weatherapp.data

sealed class WeatherData

data class CurrentLocation(
    val date: String,
    val location: String = "Choose your location"
) : WeatherData()