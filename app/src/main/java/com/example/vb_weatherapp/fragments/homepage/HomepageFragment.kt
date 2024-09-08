package com.example.vb_weatherapp.fragments.homepage

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.vb_weatherapp.R
import com.example.vb_weatherapp.data.CurrentLocation
import com.example.vb_weatherapp.data.CurrentWeather
import com.example.vb_weatherapp.data.DailyForecast
import com.example.vb_weatherapp.databinding.FragmentHomepageBinding
import com.example.vb_weatherapp.storage.SharedPreferencesManager
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class HomepageFragment : Fragment() {

    companion object {
        const val REQUEST_KEY_MANUAL_LOCATION_SEARCH = "manualLocationSearch"
        const val KEY_LOCATION_TEXT = "manualLocationSearch"
        const val KEY_LATITUDE = "manualLocationSearch"
        const val KEY_LONGITUDE = "manualLocationSearch"
    }

    private var _binding: FragmentHomepageBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val homeViewModel: HomepageViewModel by viewModel()
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val geocoder by lazy { Geocoder(requireContext()) }

    private val weatherDataAdapterHomepage = WeatherDataAdapterHomepage { dailyForecast ->
        navigateToHourlyForecast(dailyForecast)
    }

    private val sharedPreferencesManager: SharedPreferencesManager by inject()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private var isInitialLocationSet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDailyForecastAdapter()
        setObservers()
        setListeners()
        updateDateAndLocation()
        if (!isInitialLocationSet) {
            setCurrentLocation(currentLocation = sharedPreferencesManager.getCurrentLocation())
            isInitialLocationSet = true
        }
    }

    private fun setListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            setCurrentLocation(sharedPreferencesManager.getCurrentLocation())
        }
        binding.currentLocation.setOnClickListener {
            showLocationOptions()
        }
    }

    private fun setObservers() {
        with(homeViewModel) {
            currentLocation.observe(viewLifecycleOwner) {
                val currentLocationDataState = it.getContentIfNotHandled() ?: return@observe
                if (currentLocationDataState.isLoading) {
                    showLoading()
                }
                currentLocationDataState.currentLocation?.let { currentLocation ->
                    hideLoading()
                    sharedPreferencesManager.saveCurrentLocation(currentLocation)
                    setCurrentLocation(currentLocation)
                }
                currentLocationDataState.error?.let { error ->
                    hideLoading()
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
            weatherData.observe(viewLifecycleOwner) {
                val weatherDataState = it.getContentIfNotHandled() ?: return@observe
                binding.swipeRefreshLayout.isRefreshing = weatherDataState.isLoading
                weatherDataState.currentWeather?.let { currentWeather ->
                    updateCurrentWeatherUI(currentWeather)
                }
                weatherDataState.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
            dailyForecast.observe(viewLifecycleOwner) { event ->
                val dailyForecastState = event.getContentIfNotHandled() ?: return@observe
                binding.swipeRefreshLayout.isRefreshing = dailyForecastState.isLoading
                dailyForecastState.forecast?.let { forecasts ->
                    weatherDataAdapterHomepage.submitList(forecasts)
                }
                dailyForecastState.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setDailyForecastAdapter() {
        binding.recyclerViewDailyForecast.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDailyForecast.adapter = weatherDataAdapterHomepage
    }

    private fun setCurrentLocation(currentLocation: CurrentLocation? = null) {
        currentLocation?.let {
            updateDateAndLocation(it)
            getWeatherData(currentLocation = it)
            getDailyForecast(currentLocation = it)
        }
    }

    private fun getCurrentLocation() {
        homeViewModel.getCurrentLocation(fusedLocationProviderClient, geocoder)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun proceedWithCurrentLocation() {
        if (isLocationPermissionGranted()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun showLocationOptions() {
        val options = arrayOf("Current Location", "Search Manually")
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Choose Location Method")
            setItems(options) { _, which ->
                when (which) {
                    0 -> proceedWithCurrentLocation()
                    1 -> startManualLocationSearch()
                }
            }
            show()
        }
    }

    private fun showLoading() {
        with(binding) {
            layoutCurrentWeather.root.visibility = View.GONE
            recyclerViewDailyForecast.visibility = View.GONE
            swipeRefreshLayout.isEnabled = false
            swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun hideLoading() {
        with(binding) {
            layoutCurrentWeather.root.visibility = View.VISIBLE
            recyclerViewDailyForecast.visibility = View.VISIBLE
            swipeRefreshLayout.isEnabled = true
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun startManualLocationSearch() {
        startListeningManualLocationSelection()
        findNavController().navigate(R.id.action_homepageFragment_to_locationFragment)
    }

    private fun startListeningManualLocationSelection() {
        setFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH) { _, bundle ->
            stopListeningManualLocationSelection()
            val currentLocation = CurrentLocation(
                location = bundle.getString(KEY_LOCATION_TEXT) ?: "N/A",
                latitude = bundle.getDouble(KEY_LATITUDE),
                longitude = bundle.getDouble(KEY_LONGITUDE)
            )
            sharedPreferencesManager.saveCurrentLocation(currentLocation)
            setCurrentLocation(currentLocation)
        }
    }

    private fun stopListeningManualLocationSelection() {
        clearFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH)
    }

    private fun getWeatherData(currentLocation: CurrentLocation) {
        if (currentLocation.latitude != null && currentLocation.longitude != null) {
            homeViewModel.getWeatherData(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
        }
    }

    private fun getDailyForecast(currentLocation: CurrentLocation) {
        if (currentLocation.latitude != null && currentLocation.longitude != null) {
            homeViewModel.getDailyForecast(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
        }
    }

    private fun updateCurrentWeatherUI(currentWeather: CurrentWeather) {
        binding.layoutCurrentWeather.apply {
            textTemperature.text = "${currentWeather.temperature}°C"
            imageIcon.load("https:${currentWeather.icon}") { crossfade(true) }
            textWind.text = "${currentWeather.wind} km/h"
            textHumidity.text = "${currentWeather.humidity}%"
            textChanceOfRain.text = "${currentWeather.chanceOfRain}%"
        }
    }

    private fun updateDateAndLocation(currentLocation: CurrentLocation? = null) {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.textCurrentDate.text = currentDate
        binding.textCurrentLocation.text = currentLocation?.location ?: "Choose your location"
    }

    private fun navigateToHourlyForecast(dailyForecast: DailyForecast) {
        val bundle = Bundle().apply {
            putString("date", dailyForecast.date)
            putDouble("latitude", sharedPreferencesManager.getCurrentLocation()?.latitude ?: 0.0)
            putDouble("longitude", sharedPreferencesManager.getCurrentLocation()?.longitude ?: 0.0)
        }
        findNavController().navigate(R.id.action_homepageFragment_to_homeFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}