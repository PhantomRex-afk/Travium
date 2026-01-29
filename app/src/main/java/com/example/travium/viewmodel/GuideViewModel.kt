package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travium.model.UserModel
import com.example.travium.repository.UserRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuideViewModel(private val repository: UserRepo) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    fun registerGuide(
        fullName: String,
        email: String,
        password: String,
        dob: String,
        gender: String,
        phone: String,
        location: String,
        experience: String,
        specialties: String,
        bio: String
    ) {
        _loading.value = true
        // First create auth account
        repository.register(email, password) { success, userId, message ->
            if (success) {
                val guideUser = UserModel(
                    userId = userId,
                    email = email,
                    fullName = fullName,
                    dob = dob,
                    gender = gender,
                    phoneNumber = phone,
                    location = location,
                    yearsOfExperience = experience,
                    specialties = specialties,
                    bio = bio,
                    isGuide = true,
                    status = "pending"
                )
                // Then add details to database
                repository.addUserToDatabase(userId, guideUser) { dbSuccess, dbMessage ->
                    _loading.value = false
                    if (dbSuccess) {
                        _status.value = "success: Application submitted!"
                    } else {
                        _status.value = dbMessage
                    }
                }
            } else {
                _loading.value = false
                _status.value = message
            }
        }
    }

    fun fetchGuideProfile(userId: String) {
        _loading.value = true
        repository.getUserById(userId) { user ->
            _loading.value = false
            if (user != null) {
                _userProfile.value = user
            } else {
                _status.value = "Profile not found"
            }
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}
