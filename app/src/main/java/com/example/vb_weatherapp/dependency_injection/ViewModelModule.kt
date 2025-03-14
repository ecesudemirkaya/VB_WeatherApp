package com.example.vb_weatherapp.dependency_injection

import com.example.vb_weatherapp.fragments.home.HomeViewModel
import com.example.vb_weatherapp.fragments.homepage.HomepageViewModel
import com.example.vb_weatherapp.fragments.location.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LocationViewModel(weatherDataRepository = get()) }
    viewModel { HomepageViewModel(weatherDataRepository = get()) }
    viewModel { HomeViewModel(weatherDataRepository = get()) }
}