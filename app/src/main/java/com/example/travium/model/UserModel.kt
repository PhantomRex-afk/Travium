package com.example.travium.model

data class UserModel(
    val userId : String = "",
    val email : String = "",
    val fullName : String = "",
    val username: String = "",
    val dob : String = "",
    val gender: String = "",
    val phoneNumber: String = "",
    val country : String = "",
    val location: String = "", // Used for District in guides
    val bio: String = "",
    val profileImageUrl: String = "",
    val isGuide: Boolean = false,
    val yearsOfExperience: String = "",
    val specialties: String = "",
    val status: String = "", // pending, approved, rejected
    val timestamp: Long = System.currentTimeMillis()
)
