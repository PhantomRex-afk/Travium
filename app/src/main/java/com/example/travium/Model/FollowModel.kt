package com.example.travium.model

data class FollowModel(
    val followId: String = "",
    val followerId: String = "",       // followed
    val followingId: String = "",      // following
    val timestamp: Long = System.currentTimeMillis()
) {
    // Add compound key for querying
    val followerId_followingId: String
        get() = "$followerId-$followingId"

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "followId" to followId,
            "followerId" to followerId,
            "followingId" to followingId,
            "followerId_followingId" to followerId_followingId,
            "timestamp" to timestamp
        )
    }
}