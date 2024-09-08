package com.example.vb_weatherapp.fragments.homepage

import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vb_weatherapp.data.CurrentLocation
import com.example.vb_weatherapp.data.CurrentWeather
import com.example.vb_weatherapp.data.DailyForecast
import com.example.vb_weatherapp.data.Forecast
import com.example.vb_weatherapp.data.LiveDataEvent
import com.example.vb_weatherapp.data.toDayOfWeek
import com.example.vb_weatherapp.network.repository.WeatherDataRepository
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HomepageViewModel(private val weatherDataRepository: WeatherDataRepository) : ViewModel() {

    private val _currentLocation = MutableLiveData<LiveDataEvent<CurrentLocationDataState>>()
    val currentLocation: LiveData<LiveDataEvent<CurrentLocationDataState>> get() = _currentLocation

    private val _weatherData = MutableLiveData<LiveDataEvent<WeatherDataState>>()
    val weatherData: LiveData<LiveDataEvent<WeatherDataState>> get() = _weatherData

    private val _dailyForecast = MutableLiveData<LiveDataEvent<DailyForecastDataState>>()
    val dailyForecast: LiveData<LiveDataEvent<DailyForecastDataState>> get() = _dailyForecast

    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        geocoder: Geocoder
    ) {
        viewModelScope.launch {
            emitCurrentLocationUiState(isLoading = true)
            weatherDataRepository.getCurrentLocation(
                fusedLocationProviderClient = fusedLocationProviderClient,
                onSuccess = { currentLocation ->
                    updateAddressText(currentLocation, geocoder)
                },
                onFailure = {
                    emitCurrentLocationUiState(error = "Unable to fetch current location")
                }
            )
        }
    }

    private fun updateAddressText(currentLocation: CurrentLocation, geocoder: Geocoder) {
        viewModelScope.launch {
            runCatching {
                weatherDataRepository.updateAddressText(currentLocation, geocoder)
            }.onSuccess { location ->
                emitCurrentLocationUiState(currentLocation = location)
            }.onFailure {
                emitCurrentLocationUiState(
                    currentLocation = currentLocation.copy(
                        location = "N/A"
                    )
                )
            }
        }
    }

    fun getWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            emitWeatherDataUiState(isLoading = true)
            weatherDataRepository.getWeatherData(latitude, longitude)?.let { weatherData ->
                emitWeatherDataUiState(
                    currentWeather = CurrentWeather(
                        icon = weatherData.current.condition.icon,
                        temperature = weatherData.current.temperature,
                        wind = weatherData.current.wind,
                        humidity = weatherData.current.humidity,
                        chanceOfRain = weatherData.forecast.forecastDay.first().day.chanceOfRain
                    ),
                    forecast = weatherData.forecast.forecastDay.first().hour.map {
                        Forecast(
                            time = getForecastTime(it.time),
                            temperature = it.temperature,
                            icon = it.condition.icon
                        )
                    }
                )
            } ?: emitWeatherDataUiState(error = "Unable to fetch weather data")
        }
    }

    fun getDailyForecast(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            emitDailyForecastUiState(isLoading = true)
            weatherDataRepository.getWeeklyForecast(latitude, longitude)?.let { weatherData ->
                emitDailyForecastUiState(
                    forecast = weatherData.forecast.forecastDay.map { day ->
                        DailyForecast(
                            date = day.date,
                            dayOfWeek = day.date.toDayOfWeek(),
                            avgTemp = day.day.avgTemp,
                            minTemp = day.day.minTemp,
                            maxTemp = day.day.maxTemp,
                            icon = day.day.condition.icon,
                            chanceOfRain = day.day.chanceOfRain
                        )
                    }
                )
            } ?: emitDailyForecastUiState(error = "Unable to fetch daily forecast")
        }
    }

    private fun emitCurrentLocationUiState(
        isLoading: Boolean = false,
        currentLocation: CurrentLocation? = null,
        error: String? = null
    ) {
        val currentLocationDataState = CurrentLocationDataState(isLoading, currentLocation, error)
        _currentLocation.value = LiveDataEvent(currentLocationDataState)
    }

    private fun emitWeatherDataUiState(
        isLoading: Boolean = false,
        currentWeather: CurrentWeather? = null,
        forecast: List<Forecast>? = null,
        error: String? = null
    ) {
        val weatherDataState = WeatherDataState(isLoading, currentWeather, forecast, error)
        _weatherData.value = LiveDataEvent(weatherDataState)
    }

    private fun emitDailyForecastUiState(
        isLoading: Boolean = false,
        forecast: List<DailyForecast>? = null,
        error: String? = null
    ) {
        val dailyForecastDataState = DailyForecastDataState(isLoading, forecast, error)
        _dailyForecast.value = LiveDataEvent(dailyForecastDataState)
    }

    private fun getForecastTime(dateTime: String): String {
        val pattern = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = pattern.parse(dateTime) ?: return dateTime
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    data class CurrentLocationDataState(
        val isLoading: Boolean,
        val currentLocation: CurrentLocation?,
        val error: String?
    )

    data class WeatherDataState(
        val isLoading: Boolean,
        val currentWeather: CurrentWeather?,
        val forecast: List<Forecast>?,
        val error: String?
    )

    data class DailyForecastDataState(
        val isLoading: Boolean,
        val forecast: List<DailyForecast>?,
        val error: String?
    )
}
