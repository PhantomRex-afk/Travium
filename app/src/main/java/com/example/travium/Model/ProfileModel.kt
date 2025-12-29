package com.example.travium.Model

data class ProfileModel(
    val username: String,
    val category: String,
    val bio: String,
    val subtitle: String,
    val profileImageUri: String? = null,
    val postsCount: String,
    val followersCount: String,
    val followingCount: String,
    val events: List<EventModel>
)

data class EventModel(
    val id: String,
    val title: String,
    val imageRes: Int
)
