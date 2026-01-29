package com.example.travium.model

data class SettingModel(
    val userId: String = "",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val isProfilePublic: Boolean = true,
    val language: String = "English",
    val travelPlanVisibility: String = "Public", // Public, Friends, Only Me
    val locationSharing: Boolean = true,
    val currency: String = "USD",
    val allowGroupInvites: Boolean = true,
    val autoSyncTravelData: Boolean = true
)
