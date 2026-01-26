package com.example.travium.Repository

import com.example.travium.Model.SettingModel
import com.google.firebase.database.FirebaseDatabase

class SettingRepoImpl : SettingRepo {
    private val database = FirebaseDatabase.getInstance().getReference("settings")

    override fun saveSettings(
        userId: String,
        settings: SettingModel,
        callback: (Boolean, String) -> Unit
    ) {
        database.child(userId).setValue(settings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Settings saved successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to save settings")
                }
            }
    }

    override fun getSettings(
        userId: String,
        callback: (Boolean, String, SettingModel?) -> Unit
    ) {
        database.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val settings = snapshot.getValue(SettingModel::class.java)
                callback(true, "Settings fetched", settings)
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Failed to fetch settings", null)
            }
    }
}
