package com.example.travium.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.ChatListItemModel
import com.example.travium.repository.ChatRepo

class ChatListViewModel(private val chatRepo: ChatRepo, private val currentUserId: String) : ViewModel() {

    private val _chatList = MutableLiveData<List<ChatListItemModel>>()
    val chatList: LiveData<List<ChatListItemModel>> = _chatList

    init {
        fetchChatList()
    }

    private fun fetchChatList() {
        chatRepo.getChatList(currentUserId) { list ->
            _chatList.postValue(list.sortedByDescending { it.timestamp })
        }
    }
}

