// HotelRepoImpl.kt
package com.example.travium.repository

import android.content.Context
import android.net.Uri
import com.example.travium.model.HotelModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.delay

class HotelRepoImpl : HotelRepo {

    // FIX: Use mutableListOf instead of listOf
    private val simulatedHotels = mutableListOf<HotelModel>(
        HotelModel(
            hotelId = "1",
            name = "Grand Hotel Kathmandu",
            description = "Luxury hotel in the heart of Kathmandu",
            address = "Thamel, Kathmandu, Nepal",
            contactNumber = "+977-1-1234567",
            priceRange = "$100-$300",
            amenities = listOf("WiFi", "Pool", "Spa", "Gym", "Restaurant"),
            latitude = 27.7172,
            longitude = 85.3240,
            imageUrls = listOf("https://example.com/hotel1.jpg"),
            rating = 4.5,
            reviewCount = 120,
            createdAt = System.currentTimeMillis(),
            ownerId = "admin"
        ),
        HotelModel(
            hotelId = "2",
            name = "Mountain View Resort",
            description = "Beautiful resort with mountain views",
            address = "Pokhara, Nepal",
            contactNumber = "+977-61-7654321",
            priceRange = "$80-$200",
            amenities = listOf("WiFi", "Free Breakfast", "Parking", "Garden"),
            latitude = 28.2096,
            longitude = 83.9856,
            imageUrls = listOf("https://example.com/hotel2.jpg"),
            rating = 4.2,
            reviewCount = 89,
            createdAt = System.currentTimeMillis() - 86400000,
            ownerId = "admin"
        )
    )

    override suspend fun getAllHotels(onResult: (Result<List<HotelModel>>) -> Unit) {
        delay(500)
        onResult(Result.success(simulatedHotels.toList())) // Convert to immutable list
    }

    override suspend fun getHotelById(hotelId: String, onResult: (Result<HotelModel>) -> Unit) {
        delay(300)
        val hotel = simulatedHotels.find { it.hotelId == hotelId }
        if (hotel != null) {
            onResult(Result.success(hotel))
        } else {
            onResult(Result.failure(Exception("Hotel not found")))
        }
    }

    override suspend fun createHotel(hotel: HotelModel, onResult: (Result<HotelModel>) -> Unit) {
        delay(1000)
        val newHotel = hotel.copy(hotelId = (simulatedHotels.size + 1).toString())
        simulatedHotels.add(newHotel)
        onResult(Result.success(newHotel))
    }

    override suspend fun updateHotel(hotelId: String, hotel: HotelModel, onResult: (Result<Unit>) -> Unit) {
        delay(500)
        val index = simulatedHotels.indexOfFirst { it.hotelId == hotelId }
        if (index != -1) {
            simulatedHotels[index] = hotel.copy(hotelId = hotelId)
            onResult(Result.success(Unit))
        } else {
            onResult(Result.failure(Exception("Hotel not found")))
        }
    }

    override suspend fun deleteHotel(hotelId: String, onResult: (Result<Unit>) -> Unit) {
        delay(500)
        val removed = simulatedHotels.removeAll { it.hotelId == hotelId }
        if (removed) {
            onResult(Result.success(Unit))
        } else {
            onResult(Result.failure(Exception("Hotel not found")))
        }
    }

    override suspend fun getHotelsByOwner(ownerId: String, onResult: (Result<List<HotelModel>>) -> Unit) {
        delay(300)
        val ownerHotels = simulatedHotels.filter { it.ownerId == ownerId }
        onResult(Result.success(ownerHotels))
    }

    override suspend fun searchHotels(
        city: String?,
        country: String?,
        minPrice: Double?,
        maxPrice: Double?,
        guests: Int?,
        onResult: (Result<List<HotelModel>>) -> Unit
    ) {
        delay(500)
        var results = simulatedHotels.toList()

        if (city != null) {
            results = results.filter { it.address.contains(city, ignoreCase = true) }
        }

        onResult(Result.success(results))
    }

    override suspend fun updateRoomAvailability(
        hotelId: String,
        roomTypeId: String,
        bookedRooms: Int,
        onResult: (Result<Unit>) -> Unit
    ) {
        delay(300)
        onResult(Result.success(Unit))
    }

    override fun listenToHotelsByOwner(ownerId: String, onUpdate: (List<HotelModel>) -> Unit) {
        // Simulate real-time updates
        val ownerHotels = simulatedHotels.filter { it.ownerId == ownerId }
        onUpdate(ownerHotels)
    }

    override fun stopListening() {
        // Stop any listeners
    }

    override fun documentToHotel(document: DocumentSnapshot): HotelModel? {
        // Convert Firestore document to HotelModel
        return try {
            document.toObject(HotelModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Additional methods for AddHotelActivity compatibility
    suspend fun getAllHotels(): List<HotelModel> {
        delay(500)
        return simulatedHotels.toList() // Return immutable copy
    }

    suspend fun addHotel(
        context: Context,
        hotel: HotelModel,
        imageUris: List<Uri>
    ): Boolean {
        delay(1000)
        val newHotel = hotel.copy(
            hotelId = (simulatedHotels.size + 1).toString(),
            imageUrls = imageUris.map { it.toString() } // Convert URIs to strings
        )
        simulatedHotels.add(newHotel)
        return true
    }

    suspend fun updateHotel(
        context: Context,
        hotelId: String,
        hotel: HotelModel,
        imageUris: List<Uri> = emptyList()
    ): Boolean {
        delay(500)
        val index = simulatedHotels.indexOfFirst { it.hotelId == hotelId }
        if (index != -1) {
            val updatedHotel = if (imageUris.isNotEmpty()) {
                hotel.copy(imageUrls = imageUris.map { it.toString() })
            } else {
                hotel
            }
            simulatedHotels[index] = updatedHotel
            return true
        }
        return false
    }
}