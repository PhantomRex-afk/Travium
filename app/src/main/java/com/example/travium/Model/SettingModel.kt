package com.example.travium.Model

data class SettingModel(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "English",
    val travelPlanVisibility: String = "Public", // Public, Friends, Private
    val locationSharing: Boolean = true,
    val currency: String = "USD"
)
