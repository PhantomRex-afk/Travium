package com.example.travium.repository

import com.example.travium.model.ChatListItemModel
import com.example.travium.model.ChatMessageModel

interface ChatRepo {

    fun getChatList(
        currentUserId: String,
        callback: (List<ChatListItemModel>) -> Unit
    )

    fun getMessages(
        chatId: String,
        callback: (List<ChatMessageModel>) -> Unit
    )

    fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        message: String
    )
}