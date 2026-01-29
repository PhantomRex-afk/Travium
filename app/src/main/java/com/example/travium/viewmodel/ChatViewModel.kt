package com.example.travium.viewmodel

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travium.model.ChatMessage
import com.example.travium.model.ChatRoom
import com.example.travium.model.GroupChat
import com.example.travium.repository.ChatRepository
import com.example.travium.repository.GroupChatRepo
import com.example.travium.utils.ChatItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(private val chatRepository: ChatRepository,
                    private val groupChatRepository: GroupChatRepo) : ViewModel() {

    private val _chatRooms = MutableLiveData<List<ChatRoom>>()
    val chatRooms: LiveData<List<ChatRoom>> = _chatRooms

    private val _allChats = MutableLiveData<List<ChatItem>>()
    val allChats: LiveData<List<ChatItem>> = _allChats

    private val _messages = MutableLiveData<List<ChatMessage>?>()
    val messages: LiveData<List<ChatMessage>> = _messages as LiveData<List<ChatMessage>>

    private val _currentChatRoom = MutableLiveData<ChatRoom?>(null)
    val currentChatRoom: LiveData<ChatRoom?> = _currentChatRoom

    private val _isTyping = MutableLiveData<Pair<String, Boolean>?>(null)
    val isTyping: LiveData<Pair<String, Boolean>?> = _isTyping

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _messageSent = MutableLiveData<Boolean>(false)
    val messageSent: LiveData<Boolean> = _messageSent

    private var currentChatId: String? = null
    private var typingListenerActive = false

    private val _uploadProgress = MutableLiveData<Double>(0.0)
    val uploadProgress: LiveData<Double> = _uploadProgress

    private val _isUploading = MutableLiveData<Boolean>(false)
    val isUploading: LiveData<Boolean> = _isUploading


    fun loadChatRooms(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            chatRepository.getChatRooms(userId) { success, message, chatRooms ->
                _loading.value = false
                if (success && chatRooms != null) {
                    _chatRooms.value = chatRooms
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun getOrCreateChatRoom(
        participant1Id: String,
        participant2Id: String,
        participant1Name: String,
        participant2Name: String,
        participant1Photo: String,
        participant2Photo: String,
        onSuccess: (ChatRoom) -> Unit = {}
    ) {
        _loading.value = true
        viewModelScope.launch {
            chatRepository.getOrCreateChatRoom(
                participant1Id = participant1Id,
                participant2Id = participant2Id,
                participant1Name = participant1Name,
                participant2Name = participant2Name,
                participant1Photo = participant1Photo,
                participant2Photo = participant2Photo
            ) { success, message, chatRoom ->
                _loading.value = false
                if (success && chatRoom != null) {
                    _currentChatRoom.value = chatRoom
                    currentChatId = chatRoom.chatId
                    onSuccess(chatRoom)
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun loadMessages(chatId: String) {
        _loading.value = true
        currentChatId = chatId
        viewModelScope.launch {
            chatRepository.getMessages(chatId) { success, message, messages ->
                _loading.value = false
                if (success && messages != null) {
                    _messages.value = messages.sortedBy { it.timestamp }
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String,
        messageText: String,
        messageType: String = "text",
        mediaUrl: String = ""
    ) {
        if (messageText.trim().isEmpty() && mediaUrl.isEmpty()) return

        val message = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            senderName = senderName,
            receiverName = receiverName,
            messageText = messageText,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            messageType = messageType,
            mediaUrl = mediaUrl
        )

        viewModelScope.launch {
            chatRepository.sendMessage(chatId, message) { success, errorMessage ->
                if (success) {
                    _messageSent.value = true
                    _messageSent.value = false
                } else {
                    _error.value = errorMessage
                }
            }
        }
    }

    fun listenForNewMessages(chatId: String) {
        chatRepository.listenForNewMessages(chatId) { newMessage ->
            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()

            if (!currentMessages.any { it.messageId == newMessage.messageId }) {
                currentMessages.add(newMessage)
                _messages.value = currentMessages.sortedBy { it.timestamp }
            }
        }
    }

    fun markMessagesAsRead(chatId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(chatId, userId) { success, message ->
                if (!success) {
                    _error.value = message
                }
            }
        }
    }

    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        chatRepository.updateTypingStatus(chatId, userId, isTyping)
    }

    fun listenForTypingStatus(chatId: String) {
        if (!typingListenerActive) {
            chatRepository.listenForTypingStatus(chatId) { userId, isTyping ->
                _isTyping.value = Pair(userId, isTyping)
            }
            typingListenerActive = true
        }
    }

    fun deleteMessage(messageId: String, chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId, chatId) { success, message ->
                if (success) {
                    val updatedMessages = _messages.value?.filterNot { it.messageId == messageId }
                    _messages.value = updatedMessages
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearCurrentChat() {
        _currentChatRoom.value = null
        _messages.value = emptyList()
        _isTyping.value = null
        currentChatId = null
        typingListenerActive = false
    }

    fun refreshMessages() {
        currentChatId?.let { loadMessages(it) }
    }

    fun updateChatRooms() {
        _currentChatRoom.value?.let { room ->
            val currentUser = getCurrentUserId()
            currentUser?.let { userId ->
                loadChatRooms(userId)
            }
        }
    }

    private fun getCurrentUserId(): String? {
        return null
    }

    fun loadAllChats(userId: String) {
        _loading.value = true

        viewModelScope.launch {
            // We need to wait for BOTH 1-on-1 and Groups to load
            val privateChatsDeferred = async {
                fetchPrivateChatsSuspend(userId)
            }

            val groupChatsDeferred = async {
                fetchGroupChatsSuspend(userId)
            }

            val privateChats = privateChatsDeferred.await()
            val groupChats = groupChatsDeferred.await()

            // Convert to Unified ChatItems
            val chatItems = mutableListOf<ChatItem>()

            // Convert Private Chats
            privateChats.forEach { room ->
                val otherId = if (room.participant1Id == userId) room.participant2Id else room.participant1Id
                chatItems.add(ChatItem.Private(room, otherId))
            }

            // Convert Group Chats
            groupChats.forEach { group ->
                chatItems.add(ChatItem.Group(group))
            }

            // Sort by time descending (newest first)
            val sortedChats = chatItems.sortedByDescending { it.lastMessageTime }

            _allChats.value = sortedChats
            _loading.value = false
        }
    }

    // Helper to wrap your callback-based repo in coroutines
    private suspend fun fetchPrivateChatsSuspend(userId: String): List<ChatRoom> = suspendCoroutine { cont ->
        chatRepository.getChatRooms(userId) { success, _, rooms ->
            cont.resume(if (success) rooms ?: emptyList() else emptyList())
        }
    }

    private suspend fun fetchGroupChatsSuspend(userId: String): List<GroupChat> = suspendCoroutine { cont ->
        groupChatRepository.getGroupsForUser(userId) { result ->
            result.onSuccess { cont.resume(it) }
            result.onFailure { cont.resume(emptyList()) }
        }
    }
}