package com.example.travium.model

data class NotificationModel(
    val notificationId: String = "",
    val type: String = "", // "like" or "comment"
    val fromUserId: String = "",
    val message: String = "", // For comments
    val postId: String = "",
    val timestamp: Long = 0L
)
