package com.example.travium.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.ChatMessageModel
import com.example.travium.repository.ChatRepo

class ChatViewModel(private val chatRepo: ChatRepo, private val chatId: String) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    init {
        fetchMessages()
    }

    private fun fetchMessages() {
        chatRepo.getMessages(chatId) { list ->
            _messages.postValue(list.sortedBy { it.timestamp })
        }
    }

    fun sendMessage(senderId: String, receiverId: String, message: String) {
        chatRepo.sendMessage(chatId, senderId, receiverId, message)
    }
}
