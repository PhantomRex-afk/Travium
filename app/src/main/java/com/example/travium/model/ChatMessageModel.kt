package com.example.travium.model

data class ChatMessageModel(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

