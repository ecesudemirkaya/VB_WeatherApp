package com.example.vb_weatherapp.data

data class RemoteLocation(
    val name: String,
    val region: String,
    val country: String,
    val geometry: Geometry
) {
    val lat: Double
        get() = geometry.lat

    val lon: Double
        get() = geometry.lng
}

data class Geometry(
    val lat: Double,
    val lng: Double
)

data class OpenCageResponse(
    val results: List<OpenCageResult>
)

data class OpenCageResult(
    val formatted: String,
    val components: OpenCageComponents,
    val geometry: Geometry
)

data class OpenCageComponents(
    val city: String?,
    val state: String?,
    val country: String
)
