package com.example.travium.model

data class Comment(
    val commentId: String = "",
    val userId: String = "",
    val fullName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
