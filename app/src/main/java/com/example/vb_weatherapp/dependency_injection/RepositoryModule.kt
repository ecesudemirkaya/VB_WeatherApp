package com.example.vb_weatherapp.dependency_injection

import com.example.vb_weatherapp.network.repository.WeatherDataRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { WeatherDataRepository(weatherAPI = get()) }
}