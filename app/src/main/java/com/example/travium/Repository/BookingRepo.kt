package com.example.travium.repository


import com.example.travium.model.BookingModel
import com.example.travium.model.BookingStatus
import com.example.travium.model.PaymentStatus
import com.google.firebase.firestore.DocumentSnapshot


interface BookingRepo {
    // Create booking
    suspend fun createBooking(booking: BookingModel, onResult: (Result<BookingModel>) -> Unit)

    // Get bookings
    suspend fun getBookingById(bookingId: String, onResult: (Result<BookingModel>) -> Unit)
    suspend fun getUserBookings(userId: String, onResult: (Result<List<BookingModel>>) -> Unit)
    suspend fun getHotelBookings(hotelId: String, onResult: (Result<List<BookingModel>>) -> Unit)
    suspend fun getHotelOwnerBookings(hotelOwnerId: String, onResult: (Result<List<BookingModel>>) -> Unit)

    // Update booking
    suspend fun updateBookingStatus(
        bookingId: String,
        status: BookingStatus,
        onResult: (Result<Unit>) -> Unit
    )

    suspend fun updatePaymentStatus(
        bookingId: String,
        status: PaymentStatus,
        onResult: (Result<Unit>) -> Unit
    )

    suspend fun updateBooking(
        bookingId: String,
        booking: BookingModel,
        onResult: (Result<Unit>) -> Unit
    )

    // Cancel booking
    suspend fun cancelBooking(bookingId: String, onResult: (Result<Unit>) -> Unit)

    // Delete booking
    suspend fun deleteBooking(bookingId: String, onResult: (Result<Unit>) -> Unit)

    // Real-time listeners
    fun listenToUserBookings(userId: String, onUpdate: (List<BookingModel>) -> Unit)
    fun listenToHotelBookings(hotelId: String, onUpdate: (List<BookingModel>) -> Unit)
    fun stopListening()

    // Helper to convert document to model
    fun documentToBooking(document: DocumentSnapshot): BookingModel?
}