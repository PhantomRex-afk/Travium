// BookingRepoImpl.kt
package com.example.travium.repository

import com.example.travium.model.BookingModel
import kotlinx.coroutines.delay

class BookingRepoImpl : BookingRepo {

    private val simulatedBookings = mutableListOf<BookingModel>()

    override suspend fun createBooking(booking: BookingModel, callback: (Boolean, String) -> Unit) {
        delay(500)
        val newBooking = booking.copy(bookingId = (simulatedBookings.size + 1).toString())
        simulatedBookings.add(newBooking)
        callback(true, "Booking created successfully!")
    }

    override suspend fun getBookingById(bookingId: String, callback: (Result<BookingModel>) -> Unit) {
        delay(300)
        val booking = simulatedBookings.find { it.bookingId == bookingId }
        if (booking != null) {
            callback(Result.success(booking))
        } else {
            callback(Result.failure(Exception("Booking not found")))
        }
    }

    override suspend fun getBookingsByUser(userId: String, callback: (Result<List<BookingModel>>) -> Unit) {
        delay(300)
        val userBookings = simulatedBookings.filter { it.userId == userId }
        callback(Result.success(userBookings))
    }

    override suspend fun getBookingsByHotel(hotelId: String, callback: (Result<List<BookingModel>>) -> Unit) {
        delay(300)
        val hotelBookings = simulatedBookings.filter { it.hotelId == hotelId }
        callback(Result.success(hotelBookings))
    }

    override suspend fun updateBookingStatus(bookingId: String, status: String, callback: (Boolean, String) -> Unit) {
        delay(300)
        val index = simulatedBookings.indexOfFirst { it.bookingId == bookingId }
        if (index != -1) {
            val booking = simulatedBookings[index]
            val updatedBooking = booking.copy(status = status)
            simulatedBookings[index] = updatedBooking
            callback(true, "Booking status updated to $status")
        } else {
            callback(false, "Booking not found")
        }
    }

    override suspend fun cancelBooking(bookingId: String, callback: (Boolean, String) -> Unit) {
        updateBookingStatus(bookingId, "cancelled", callback)
    }
}