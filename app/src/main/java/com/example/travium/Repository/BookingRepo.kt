
package com.example.travium.repository

import com.example.travium.model.BookingModel

interface BookingRepo {
    suspend fun createBooking(booking: BookingModel, callback: (Boolean, String) -> Unit)
    suspend fun getBookingById(bookingId: String, callback: (Result<BookingModel>) -> Unit)
    suspend fun getBookingsByUser(userId: String, callback: (Result<List<BookingModel>>) -> Unit)
    suspend fun getBookingsByHotel(hotelId: String, callback: (Result<List<BookingModel>>) -> Unit)
    suspend fun updateBookingStatus(bookingId: String, status: String, callback: (Boolean, String) -> Unit)
    suspend fun cancelBooking(bookingId: String, callback: (Boolean, String) -> Unit)
}