package com.example.travium.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.GuideModel
import com.example.travium.repository.GuideRepo

class GuideViewModel(private val repo: GuideRepo) : ViewModel() {

    private val _allGuides = MutableLiveData<List<GuideModel>>()
    val allGuides: LiveData<List<GuideModel>> = _allGuides

    fun addGuide(context: Context, placeName: String, imageUris: List<Uri>, accommodations: String, callback: (Boolean, String) -> Unit) {
        if (placeName.isBlank()) {
            callback(false, "Please enter a place name")
            return
        }
        if (imageUris.isEmpty()) {
            callback(false, "Please select at least one picture")
            return
        }

        repo.uploadImages(context, imageUris) { urls ->
            if (urls != null) {
                val guide = GuideModel(
                    placeName = placeName,
                    imageUrls = urls,
                    accommodations = accommodations
                )
                repo.addGuide(guide, callback)
            } else {
                callback(false, "Failed to upload images")
            }
        }
    }

    fun getAllGuides() {
        repo.getAllGuides { success, message, guides ->
            if (success) {
                _allGuides.value = guides ?: emptyList()
            }
        }
    }
}
