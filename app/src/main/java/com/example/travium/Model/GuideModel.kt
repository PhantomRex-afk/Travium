package com.example.travium.model

data class GuideModel(
    val guideId: String = "",
    val placeName: String = "",
    val imageUrls: List<String> = emptyList(),
    val accommodations: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hotels: List<HotelLocation> = emptyList()
)

data class HotelLocation(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
