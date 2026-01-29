package com.example.travium.model

import java.util.*

data class HotelModel(
    val hotelId: String = UUID.randomUUID().toString(),
    val hotelName: String = "",
    val hotelOwnerId: String = "",
    val description: String = "",
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val location: Location = Location(),
    val images: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val roomTypes: List<RoomType> = emptyList(),
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val contactEmail: String = "",
    val contactPhone: String = "",
    val checkInTime: String = "14:00",
    val checkOutTime: String = "12:00",
    val policies: List<Policy> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "hotels"
    }
}

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class RoomType(
    val typeId: String = "",
    val name: String = "",
    val description: String = "",
    val pricePerNight: Double = 0.0,
    val capacity: Int = 2,
    val maxGuests: Int = 2,
    val maxRooms: Int = 10,
    val availableRooms: Int = 0,
    val amenities: List<String> = emptyList(),
    val images: List<String> = emptyList()
)

data class Policy(
    val title: String = "",
    val description: String = ""
)