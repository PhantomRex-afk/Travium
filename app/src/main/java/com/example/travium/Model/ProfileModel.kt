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
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "username" to username,
            "category" to category,
            "bio" to bio,
            "subtitle" to subtitle,
            "profileImageUri" to profileImageUri,
            "postsCount" to postsCount,
            "followersCount" to followersCount,
            "followingCount" to followingCount
        )
    }
}

data class EventModel(
    val id: String = "",
    val title: String = "",
    val imageRes: Int = 0
)
