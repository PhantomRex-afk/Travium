package com.example.travium.model

data class ChatListItemModel(
    val chatId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val userName: String = "",
    val userId: String = "" // ID other user
)

