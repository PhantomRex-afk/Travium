package com.example.travium.Repository

import android.net.Uri
import com.example.travium.Model.ProfileModel

interface ProfileRepo {
    fun updateProfile(
        userId: String,
        model: ProfileModel,
        callback: (Boolean, String) -> Unit
    )

    fun getProfile(
        userId: String,
        callback: (Boolean, String, ProfileModel?) -> Unit
    )

    fun uploadProfileImage(
        imageUri: Uri,
        callback: (Boolean, String?) -> Unit
    )
}
