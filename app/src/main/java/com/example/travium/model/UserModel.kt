package com.example.travium.model

data class UserModel(
    val userId : String = "",
    val email : String = "",
    val fullName : String = "",
    val username: String = "",
    val dob : String = "",
    val country : String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
)
