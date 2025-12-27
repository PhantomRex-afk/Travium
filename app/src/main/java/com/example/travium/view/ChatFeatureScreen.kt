package com.example.travium.view

import androidx.compose.runtime.*
import com.example.travium.model.ChatListItemModel
import com.example.travium.repository.ChatRepo
import com.example.travium.ui.chat.ChatListScreen
import com.example.travium.ui.chat.ChatScreen

@Composable
fun ChatFeatureScreen(
    chatRepo: ChatRepo,
    currentUserId: String
) {
    // Track which chat is currently open
    var selectedChat by remember { mutableStateOf<ChatListItemModel?>(null) }

    if (selectedChat == null) {
        // Show the chat list
        ChatListScreen(
            chatRepo = chatRepo,
            currentUserId = currentUserId
        ) { chat ->
            selectedChat = chat
        }
    } else {
        // Show individual chat screen
        ChatScreen(
            chatRepo = chatRepo,
            chatId = selectedChat!!.chatId,
            currentUserId = currentUserId,
            otherUserId = selectedChat!!.userId,
            onBack = { selectedChat = null } // Back to chat list
        )
    }
}