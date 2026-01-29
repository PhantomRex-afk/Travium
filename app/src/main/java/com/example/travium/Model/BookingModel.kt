package com.example.travium.model

import java.util.*

data class BookingModel(
    val bookingId: String = UUID.randomUUID().toString(),
    val hotelId: String = "",
    val hotelName: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val numberOfGuests: Int = 1,
    val numberOfRooms: Int = 1,
    val roomType: String = "",
    val totalPrice: Double = 0.0,
    val bookingStatus: BookingStatus = BookingStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val bookingDate: Long = System.currentTimeMillis(),
    val specialRequests: String = "",
    val cancellationPolicy: String = "",
    val hotelOwnerId: String = ""
) {
    companion object {
        const val COLLECTION_NAME = "bookings"
    }
}

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    REJECTED
}

enum class PaymentStatus {
    PENDING,
    PAID,
    REFUNDED,
    FAILED
}