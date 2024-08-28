package com.example.vb_weatherapp.utils

import android.app.Application
import com.example.vb_weatherapp.dependency_injection.repositoryModule
import com.example.vb_weatherapp.dependency_injection.serializerModule
import com.example.vb_weatherapp.dependency_injection.storageModule
import com.example.vb_weatherapp.dependency_injection.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppConfig: Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppConfig)
            modules(
                listOf(
                    repositoryModule,
                    viewModelModule,
                    serializerModule,
                    storageModule
                )
            )
        }
    }
}