package com.example.travium.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.travium.model.ProfileModel
import com.example.travium.repository.ProfileRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(private val repository: ProfileRepo) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileModel?>(null)
    val profile: StateFlow<ProfileModel?> = _profile.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun fetchProfile(userId: String) {
        _loading.value = true
        repository.getProfile(userId) { success, msg, model ->
            _loading.value = false
            _message.value = msg
            if (success) {
                _profile.value = model
            }
        }
    }

    fun updateProfile(userId: String, model: ProfileModel) {
        _loading.value = true
        repository.updateProfile(userId, model) { success, msg ->
            _loading.value = false
            _message.value = msg
            if (success) {
                _profile.value = model
            }
        }
    }

    fun uploadImage(uri: Uri, onComplete: (String?) -> Unit) {
        _loading.value = true
        repository.uploadProfileImage(uri) { success, url ->
            _loading.value = false
            if (success) {
                onComplete(url)
            } else {
                _message.value = "Image upload failed"
                onComplete(null)
            }
        }
    }
}