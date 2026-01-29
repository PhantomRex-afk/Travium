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
    val status: String = "pending", // pending, approved, rejected
    val timestamp: Long = System.currentTimeMillis()
)
