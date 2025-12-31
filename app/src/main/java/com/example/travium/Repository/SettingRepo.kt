package com.example.travium.Repository

import com.example.travium.Model.SettingModel

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
