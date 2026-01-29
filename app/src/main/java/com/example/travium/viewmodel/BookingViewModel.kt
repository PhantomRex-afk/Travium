// BookingViewModel.kt
package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travium.model.BookingModel
import com.example.travium.repository.BookingRepoImpl
import kotlinx.coroutines.launch

class BookingViewModel(private val bookingRepo: BookingRepoImpl) : ViewModel() {

    fun createBooking(booking: BookingModel, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            bookingRepo.createBooking(booking, callback)
        }
    }

    fun getBookingById(bookingId: String, callback: (Result<BookingModel>) -> Unit) {
        viewModelScope.launch {
            bookingRepo.getBookingById(bookingId, callback)
        }
    }

    fun getBookingsByUser(userId: String, callback: (Result<List<BookingModel>>) -> Unit) {
        viewModelScope.launch {
            bookingRepo.getBookingsByUser(userId, callback)
        }
    }

    fun getBookingsByHotel(hotelId: String, callback: (Result<List<BookingModel>>) -> Unit) {
        viewModelScope.launch {
            bookingRepo.getBookingsByHotel(hotelId, callback)
        }
    }

    fun updateBookingStatus(bookingId: String, status: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            bookingRepo.updateBookingStatus(bookingId, status, callback)
        }
    }

    fun cancelBooking(bookingId: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            bookingRepo.cancelBooking(bookingId, callback)
        }
    }
}