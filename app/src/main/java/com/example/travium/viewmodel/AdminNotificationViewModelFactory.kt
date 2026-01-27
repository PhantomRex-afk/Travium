package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.travium.repository.AdminNotificationRepo

class AdminNotificationViewModelFactory(private val repository: AdminNotificationRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminNotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminNotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
