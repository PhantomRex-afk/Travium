package com.example.travium.model

data class GuideModel(
    val guideId: String = "",
    val fullName: String = "",
    val age: String = "",
    val gender: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val yearsOfExperience: String = "",
    val specialties: String = "",
    val bio: String = "",
    val profileImageUri: String = "",
    val status: String = "pending", // pending, approved, rejected
    val timestamp: Long = System.currentTimeMillis(),
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
