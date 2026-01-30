package com.example.travium.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.GuideModel
import com.example.travium.model.HotelLocation
import com.example.travium.repository.GuideRepo

class GuideViewModel(private val repo: GuideRepo) : ViewModel() {

    private val _allGuides = MutableLiveData<List<GuideModel>>()
    val allGuides: LiveData<List<GuideModel>> = _allGuides

    fun addGuide(
        context: Context, 
        placeName: String, 
        imageUris: List<Uri>, 
        accommodations: String,
        latitude: Double,
        longitude: Double,
        hotels: List<HotelLocation>,
        callback: (Boolean, String) -> Unit
    ) {
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
                    accommodations = accommodations,
                    latitude = latitude,
                    longitude = longitude,
                    hotels = hotels
                )
                repo.addGuide(guide, callback)
            } else {
                callback(false, "Failed to upload images")
            }
        }
    }

    fun updateGuide(
        context: Context,
        guideId: String,
        placeName: String,
        imageUris: List<Uri>, // New images to upload
        existingImageUrls: List<String>, // Existing images to keep
        accommodations: String,
        latitude: Double,
        longitude: Double,
        hotels: List<HotelLocation>,
        callback: (Boolean, String) -> Unit
    ) {
        if (placeName.isBlank()) {
            callback(false, "Please enter a place name")
            return
        }

        if (imageUris.isNotEmpty()) {
            repo.uploadImages(context, imageUris) { newUrls ->
                if (newUrls != null) {
                    val updatedGuide = GuideModel(
                        guideId = guideId,
                        placeName = placeName,
                        imageUrls = existingImageUrls + newUrls,
                        accommodations = accommodations,
                        latitude = latitude,
                        longitude = longitude,
                        hotels = hotels
                    )
                    repo.updateGuide(updatedGuide, callback)
                } else {
                    callback(false, "Failed to upload new images")
                }
            }
        } else {
            val updatedGuide = GuideModel(
                guideId = guideId,
                placeName = placeName,
                imageUrls = existingImageUrls,
                accommodations = accommodations,
                latitude = latitude,
                longitude = longitude,
                hotels = hotels
            )
            repo.updateGuide(updatedGuide, callback)
        }
    }

    fun getAllGuides() {
        repo.getAllGuides { success, message, guides ->
            if (success) {
                _allGuides.value = guides ?: emptyList()
            }
        }
    }

    fun deleteGuide(guideId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteGuide(guideId, callback)
    }
}
