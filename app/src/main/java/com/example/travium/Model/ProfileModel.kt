package com.example.travium.model

data class ProfileModel(
    val id: String = "",
    val username: String = "",
    val category: String = "",
    val bio: String = "",
    val subtitle: String = "",
    val profileImageUri: String? = null,
    val postsCount: String = "0",
    val followersCount: String = "0",
    val followingCount: String = "0",
    val events: List<EventModel> = emptyList()
)

data class EventModel(
    val id: String = "",
    val title: String = "",
    val imageRes: Int = 0
)
