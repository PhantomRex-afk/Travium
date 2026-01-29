// HotelViewModel.kt
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

// Rename to avoid conflict with AddHotelViewModel
class HotelViewModel(private val hotelRepo: HotelRepoImpl) : ViewModel() {

    private val _allHotels = MutableStateFlow<List<HotelModel>>(emptyList())
    val allHotels: StateFlow<List<HotelModel>> = _allHotels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedHotel = MutableStateFlow<HotelModel?>(null)
    val selectedHotel: StateFlow<HotelModel?> = _selectedHotel.asStateFlow()

    init {
        getAllHotels()
    }

    fun getAllHotels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                hotelRepo.getAllHotels { result ->
                    result.onSuccess { hotels ->
                        _allHotels.value = hotels
                    }.onFailure { error ->
                        error.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getHotelById(hotelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                hotelRepo.getHotelById(hotelId) { result ->
                    result.onSuccess { hotel ->
                        _selectedHotel.value = hotel
                    }.onFailure { error ->
                        error.printStackTrace()
                        _selectedHotel.value = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _selectedHotel.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                    getAllHotels() // Refresh the list
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

    fun updateHotel(
        context: Context,
        hotelId: String,
        hotel: HotelModel,
        imageUris: List<Uri> = emptyList(),
        callback: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = hotelRepo.updateHotel(context, hotelId, hotel, imageUris)
                if (success) {
                    getAllHotels()
                    callback(true, "Hotel updated successfully!")
                } else {
                    callback(false, "Failed to update hotel")
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHotel(
        hotelId: String,
        callback: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                hotelRepo.deleteHotel(hotelId) { result ->
                    result.onSuccess {
                        getAllHotels() // Refresh the list
                        callback(true, "Hotel deleted successfully!")
                    }.onFailure { error ->
                        callback(false, "Failed to delete hotel: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchHotels(
        city: String? = null,
        country: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        guests: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                hotelRepo.searchHotels(city, country, minPrice, maxPrice, guests) { result ->
                    result.onSuccess { hotels ->
                        // For search, you might want to store in a separate state
                        // For now, we'll just update the main list
                        _allHotels.value = hotels
                    }.onFailure { error ->
                        error.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper method to get hotel by ID from current state
    fun getHotelByIdFromState(hotelId: String): HotelModel? {
        return _allHotels.value.find { it.hotelId == hotelId }
    }
}