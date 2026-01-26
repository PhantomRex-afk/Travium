package com.example.travium.model

data class GuideModel(
    val guideId: String = "",
    val placeName: String = "",
    val imageUrls: List<String> = emptyList(),
    val accommodations: String = ""
)
