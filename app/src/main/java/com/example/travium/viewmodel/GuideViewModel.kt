package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travium.model.GuideModel
import com.example.travium.repository.GuideRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuideViewModel(private val repository: GuideRepo) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _guideProfile = MutableStateFlow<GuideModel?>(null)
    val guideProfile: StateFlow<GuideModel?> = _guideProfile.asStateFlow()

    fun registerGuide(
        fullName: String,
        age: String,
        gender: String,
        email: String,
        phone: String,
        location: String,
        experience: String,
        specialties: String,
        bio: String
    ) {
        val guide = GuideModel(
            fullName = fullName,
            age = age,
            gender = gender,
            email = email,
            phoneNumber = phone,
            location = location,
            yearsOfExperience = experience,
            specialties = specialties,
            bio = bio
        )

        _loading.value = true
        repository.registerGuide(guide) { success, message ->
            _loading.value = false
            _status.value = message
        }
    }

    fun fetchGuideProfile(guideId: String) {
        _loading.value = true
        repository.getGuide(guideId) { guide, error ->
            _loading.value = false
            if (guide != null) {
                _guideProfile.value = guide
            } else {
                _status.value = error
            }
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}
