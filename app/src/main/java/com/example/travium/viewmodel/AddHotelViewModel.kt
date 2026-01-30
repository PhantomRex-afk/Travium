// AddHotelViewModel.kt
package com.example.travium.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travium.model.HotelModel
import com.example.travium.repository.HotelRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// This ViewModel is specifically for AddHotelActivity
class AddHotelViewModel(private val hotelRepo: HotelRepoImpl) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun addHotel(
        context: Context,
        hotel: HotelModel,
        imageUris: List<Uri>,
        callback: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = hotelRepo.addHotel(context, hotel, imageUris)
                if (success) {
                    callback(true, "Hotel added successfully!")
                } else {
                    callback(false, "Failed to add hotel")
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}