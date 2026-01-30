package com.example.travium.model

data class AdminNotificationModel(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
