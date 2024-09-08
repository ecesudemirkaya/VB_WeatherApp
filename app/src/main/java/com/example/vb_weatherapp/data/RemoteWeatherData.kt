package com.example.vb_weatherapp.data

import com.google.gson.annotations.SerializedName

data class RemoteWeatherData(
    val current: CurrentWeatherRemote,
    val forecast: ForecastRemote
)

data class CurrentWeatherRemote(
    @SerializedName("temp_c") val temperature: Float,
    val condition: WeatherConditionRemote,
    @SerializedName("wind_kph") val wind: Float,
    val humidity: Int
)

data class ForecastRemote(
    @SerializedName("forecastday") val forecastDay: List<ForecastDayRemote>
)

data class ForecastDayRemote(
    @SerializedName("date") val date: String,
    val day: DayRemote,
    val hour: List<ForecastHourRemote>
)

data class DayRemote(
    @SerializedName("avgtemp_c") val avgTemp: Float,
    @SerializedName("mintemp_c") val minTemp: Float,
    @SerializedName("maxtemp_c") val maxTemp: Float,
    @SerializedName("daily_chance_of_rain") val chanceOfRain: Int,
    val condition: WeatherConditionRemote
)

data class ForecastHourRemote(
    val time: String,
    @SerializedName("temp_c") val temperature: Float,
    @SerializedName("feelslike_c") val chanceOfRain: Float,
    @SerializedName("feelslike") val feelsLikeTemperature: Float,
    val condition: WeatherConditionRemote
)

data class WeatherConditionRemote(
    val icon: String
)
