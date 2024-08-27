package com.example.vb_weatherapp.dependency_injection

import com.example.vb_weatherapp.fragments.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(weatherDataRepository = get()) }
}