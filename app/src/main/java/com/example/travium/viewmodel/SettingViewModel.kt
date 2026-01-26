package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travium.Model.SettingModel
import com.example.travium.Repository.SettingRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingViewModel(private val repository: SettingRepo) : ViewModel() {

    private val _settings = MutableStateFlow<SettingModel?>(null)
    val settings: StateFlow<SettingModel?> = _settings.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun fetchSettings(userId: String) {
        _loading.value = true
        repository.getSettings(userId) { success, msg, model ->
            _loading.value = false
            _message.value = msg
            if (success && model != null) {
                _settings.value = model
            } else if (success && model == null) {
                // If no settings exist yet, we can initialize with a default model
                _settings.value = SettingModel(userId = userId)
            }
        }
    }

    fun saveSettings(userId: String, model: SettingModel) {
        _loading.value = true
        repository.saveSettings(userId, model) { success, msg ->
            _loading.value = false
            _message.value = msg
            if (success) {
                _settings.value = model
            }
        }
    }

    fun updateNotificationSetting(userId: String, enabled: Boolean) {
        val currentSettings = _settings.value ?: SettingModel(userId = userId)
        val updatedSettings = currentSettings.copy(notificationsEnabled = enabled)
        saveSettings(userId, updatedSettings)
    }

    fun updateDarkModeSetting(userId: String, enabled: Boolean) {
        val currentSettings = _settings.value ?: SettingModel(userId = userId)
        val updatedSettings = currentSettings.copy(darkModeEnabled = enabled)
        saveSettings(userId, updatedSettings)
    }
}