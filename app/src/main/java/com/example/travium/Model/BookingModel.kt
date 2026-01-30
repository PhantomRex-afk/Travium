// BookingModel.kt
package com.example.travium.model

data class BookingModel(
    val bookingId: String,
    val hotelId: String,
    val hotelName: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val checkInDate: String,
    val checkOutDate: String,
    val numberOfGuests: Int,
    val numberOfRooms: Int,
    val roomType: String,
    val pricePerNight: Double,
    val totalPrice: Double,
    val status: String, // "pending", "confirmed", "cancelled", "completed"
    val specialRequests: String = "",
    val createdAt: Long = System.currentTimeMillis()
)