package com.example.travium.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travium.model.HotelModel
import com.example.travium.repository.HotelRepo
import com.example.travium.repository.HotelRepoImpl
import kotlinx.coroutines.launch

class HotelViewModel(
    private val hotelRepo: HotelRepo = HotelRepoImpl()
) : ViewModel() {

    private val _uiState = MutableLiveData<HotelUiState>(HotelUiState.Idle)
    val uiState: LiveData<HotelUiState> = _uiState

    private val _hotels = MutableLiveData<List<HotelModel>>(emptyList())
    val hotels: LiveData<List<HotelModel>> = _hotels

    private val _selectedHotel = MutableLiveData<HotelModel?>()
    val selectedHotel: LiveData<HotelModel?> = _selectedHotel

    private val _searchResults = MutableLiveData<List<HotelModel>>(emptyList())
    val searchResults: LiveData<List<HotelModel>> = _searchResults

    fun getAllHotels() {
        _uiState.value = HotelUiState.Loading

        viewModelScope.launch {
            hotelRepo.getAllHotels { result ->
                result.onSuccess { hotels ->
                    _hotels.value = hotels
                    _uiState.value = HotelUiState.Success("Hotels loaded")
                }.onFailure { exception ->
                    _uiState.value = HotelUiState.Error("Failed to load hotels: ${exception.message}")
                }
            }
        }
    }

    fun getHotelById(hotelId: String) {
        _uiState.value = HotelUiState.Loading

        viewModelScope.launch {
            hotelRepo.getHotelById(hotelId) { result ->
                result.onSuccess { hotel ->
                    _selectedHotel.value = hotel
                    _uiState.value = HotelUiState.Success("Hotel details loaded")
                }.onFailure { exception ->
                    _uiState.value = HotelUiState.Error("Failed to load hotel: ${exception.message}")
                }
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
        _uiState.value = HotelUiState.Loading

        viewModelScope.launch {
            hotelRepo.searchHotels(city, country, minPrice, maxPrice, guests) { result ->
                result.onSuccess { hotels ->
                    _searchResults.value = hotels
                    _uiState.value = HotelUiState.Success("Search completed")
                }.onFailure { exception ->
                    _uiState.value = HotelUiState.Error("Search failed: ${exception.message}")
                }
            }
        }
    }

    fun setSelectedHotel(hotel: HotelModel?) {
        _selectedHotel.value = hotel
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun clearUiState() {
        _uiState.value = HotelUiState.Idle
    }

    // Start real-time listeners for hotel owners
    fun startListeningToOwnerHotels(ownerId: String) {
        hotelRepo.listenToHotelsByOwner(ownerId) { hotels ->
            _hotels.value = hotels
        }
    }

    fun stopListening() {
        hotelRepo.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}

sealed class HotelUiState {
    object Idle : HotelUiState()
    object Loading : HotelUiState()
    data class Success(val message: String) : HotelUiState()
    data class Error(val message: String) : HotelUiState()
    data class HotelSelected(val hotel: HotelModel) : HotelUiState()
}