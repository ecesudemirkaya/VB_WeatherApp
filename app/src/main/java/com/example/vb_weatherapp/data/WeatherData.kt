package com.example.vb_weatherapp.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class WeatherData

data class CurrentLocation(
    val date: String = getCurrentDate(),
    val location: String = "Choose your location",
    val latitude: Double? = null,
    val longitude: Double? = null
) : WeatherData()

data class Forecast(
    val time: String,
    val temperature: Float,
    val icon: String
) : WeatherData()

data class CurrentWeather(
    val icon: String,
    val temperature: Float,
    val wind: Float,
    val humidity: Int,
    val chanceOfRain: Int
) : WeatherData()

data class DailyForecast(
    val date: String,
    val dayOfWeek: String,
    val avgTemp: Float,
    val minTemp: Float,
    val maxTemp: Float,
    val icon: String,
    val chanceOfRain: Int
) : WeatherData()

private fun getCurrentDate(): String {
    val currentDate = Date()
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    return formatter.format(currentDate)
}

fun String.toDayOfWeek(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val date = inputFormat.parse(this)
    return date?.let { outputFormat.format(it) } ?: this
}