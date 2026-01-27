package com.example.travium.repository

import com.example.travium.model.SettingModel

interface SettingRepo {
    fun saveSettings(
        userId: String,
        settings: SettingModel,
        callback: (Boolean, String) -> Unit
    )

    fun getSettings(
        userId: String,
        callback: (Boolean, String, SettingModel?) -> Unit
    )
}
