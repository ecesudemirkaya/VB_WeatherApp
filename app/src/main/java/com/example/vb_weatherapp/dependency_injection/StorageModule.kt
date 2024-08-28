package com.example.vb_weatherapp.dependency_injection

import com.example.vb_weatherapp.storage.SharedPreferencesManager
import org.koin.dsl.module

val storageModule = module {
    single { SharedPreferencesManager(context = get(), gson = get()) }
}