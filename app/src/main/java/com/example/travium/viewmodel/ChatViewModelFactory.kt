package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.travium.repository.ChatRepository
import com.example.travium.repository.GroupChatRepo

class ChatViewModelFactory(private val chatRepository: ChatRepository,
                           private val groupChatRepository: GroupChatRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, groupChatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}