package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travium.model.AdminNotificationModel
import com.example.travium.repository.AdminNotificationRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminNotificationViewModel(private val repository: AdminNotificationRepo) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AdminNotificationModel>>(emptyList())
    val notifications: StateFlow<List<AdminNotificationModel>> = _notifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        _loading.value = true
        repository.getAllNotifications { list, error ->
            _loading.value = false
            if (list != null) {
                _notifications.value = list
            } else {
                _message.value = error
            }
        }
    }

    fun sendNotification(title: String, message: String) {
        val notification = AdminNotificationModel(
            title = title,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        _loading.value = true
        repository.sendNotification(notification) { success, msg ->
            _loading.value = false
            _message.value = msg
            if (success) {
                fetchNotifications()
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        _loading.value = true
        repository.deleteNotification(notificationId) { success, msg ->
            _loading.value = false
            _message.value = msg
            if (success) {
                fetchNotifications()
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
