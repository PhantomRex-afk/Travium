// HotelModel.kt
package com.example.travium.model

data class HotelModel(
    val hotelId: String,
    val name: String,
    val description: String,
    val address: String,
    val contactNumber: String,
    val priceRange: String,
    val amenities: List<String>,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrls: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val createdAt: Long,
    val ownerId: String? = null
)