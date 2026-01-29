package com.example.travium.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travium.model.BookingModel
import com.example.travium.model.BookingStatus
import com.example.travium.model.PaymentStatus
import com.example.travium.repository.BookingRepo
import kotlinx.coroutines.launch
import java.util.*

class BookingViewModel(
    private val bookingRepo: BookingRepo
) : ViewModel() {
    // Remove this line: private val bookingRepo: BookingRepo = BookingRepoImpl()

    private val _uiState = MutableLiveData<BookingUiState>(BookingUiState.Idle)
    val uiState: LiveData<BookingUiState> = _uiState

    private val _userBookings = MutableLiveData<List<BookingModel>>(emptyList())
    val userBookings: LiveData<List<BookingModel>> = _userBookings

    private val _hotelBookings = MutableLiveData<List<BookingModel>>(emptyList())
    val hotelBookings: LiveData<List<BookingModel>> = _hotelBookings

    private val _selectedBooking = MutableLiveData<BookingModel?>()
    val selectedBooking: LiveData<BookingModel?> = _selectedBooking

    fun createBooking(booking: BookingModel) {
        _uiState.value = BookingUiState.Loading

        viewModelScope.launch {
            bookingRepo.createBooking(booking) { result ->
                result.onSuccess { createdBooking ->
                    _uiState.value = BookingUiState.BookingCreated(createdBooking)
                }.onFailure { exception ->
                    _uiState.value = BookingUiState.Error("Failed to create booking: ${exception.message}")
                }
            }
        }
    }

    fun getUserBookings(userId: String) {
        _uiState.value = BookingUiState.Loading

        viewModelScope.launch {
            bookingRepo.getUserBookings(userId) { result ->
                result.onSuccess { bookings ->
                    _userBookings.value = bookings
                    _uiState.value = BookingUiState.Success("Bookings loaded")
                }.onFailure { exception ->
                    _uiState.value = BookingUiState.Error("Failed to load bookings: ${exception.message}")
                }
            }
        }
    }

    fun getHotelBookings(hotelId: String) {
        _uiState.value = BookingUiState.Loading

        viewModelScope.launch {
            bookingRepo.getHotelBookings(hotelId) { result ->
                result.onSuccess { bookings ->
                    _hotelBookings.value = bookings
                    _uiState.value = BookingUiState.Success("Hotel bookings loaded")
                }.onFailure { exception ->
                    _uiState.value = BookingUiState.Error("Failed to load hotel bookings: ${exception.message}")
                }
            }
        }
    }

    fun getHotelOwnerBookings(hotelOwnerId: String) {
        _uiState.value = BookingUiState.Loading

        viewModelScope.launch {
            bookingRepo.getHotelOwnerBookings(hotelOwnerId) { result ->
                result.onSuccess { bookings ->
                    _hotelBookings.value = bookings
                    _uiState.value = BookingUiState.Success("Owner bookings loaded")
                }.onFailure { exception ->
                    _uiState.value = BookingUiState.Error("Failed to load owner bookings: ${exception.message}")
                }
            }
        }
    }

    fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        _uiState.value = BookingUiState.Loading

        viewModelScope.launch {
            bookingRepo.updateBookingStatus(bookingId, status) { result ->
                result.onSuccess {
                    _uiState.value = BookingUiState.Success("Booking status updated to $status")
                }.onFailure { exception ->
                    _uiState.value = BookingUiState.Error("Failed to update status: ${exception.message}")
                }
            }
        }
    }

    fun updatePaymentStatus(bookingId: String, status: PaymentStatus) {
        _uiState.value = BookingUiState.Loading

        viewModelScope.launch {
            bookingRepo.updatePaymentStatus(bookingId, status) { result ->
                result.onSuccess {
                    _uiState.value = BookingUiState.Success("Payment status updated to $status")
                }.onFailure { exception ->
                    _uiState.value = BookingUiState.Error("Failed to update payment: ${exception.message}")
                }
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        updateBookingStatus(bookingId, BookingStatus.CANCELLED)
    }

    fun setSelectedBooking(booking: BookingModel?) {
        _selectedBooking.value = booking
    }

    fun clearUiState() {
        _uiState.value = BookingUiState.Idle
    }

    // Start real-time listeners
    fun startListeningToUserBookings(userId: String) {
        bookingRepo.listenToUserBookings(userId) { bookings ->
            _userBookings.value = bookings
        }
    }

    fun startListeningToHotelBookings(hotelId: String) {
        bookingRepo.listenToHotelBookings(hotelId) { bookings ->
            _hotelBookings.value = bookings
        }
    }

    fun stopListening() {
        bookingRepo.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}

sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    data class Success(val message: String) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
    data class BookingCreated(val booking: BookingModel) : BookingUiState()
}