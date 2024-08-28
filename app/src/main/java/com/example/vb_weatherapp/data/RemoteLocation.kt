package com.example.vb_weatherapp.data

import android.graphics.Region

data class RemoteLocation(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double
)
