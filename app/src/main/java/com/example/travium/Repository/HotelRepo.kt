package com.example.travium.repository

import com.example.travium.model.HotelModel
import com.example.travium.model.RoomType
import com.google.firebase.firestore.DocumentSnapshot

interface HotelRepo {
    // Create hotel
    suspend fun createHotel(hotel: HotelModel, onResult: (Result<HotelModel>) -> Unit)

    // Get hotels
    suspend fun getHotelById(hotelId: String, onResult: (Result<HotelModel>) -> Unit)
    suspend fun getAllHotels(onResult: (Result<List<HotelModel>>) -> Unit)
    suspend fun getHotelsByOwner(ownerId: String, onResult: (Result<List<HotelModel>>) -> Unit)
    suspend fun searchHotels(
        city: String? = null,
        country: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        guests: Int? = null,
        onResult: (Result<List<HotelModel>>) -> Unit
    )

    // Update hotel
    suspend fun updateHotel(hotelId: String, hotel: HotelModel, onResult: (Result<Unit>) -> Unit)
    suspend fun updateRoomAvailability(
        hotelId: String,
        roomTypeId: String,
        bookedRooms: Int,
        onResult: (Result<Unit>) -> Unit
    )

    // Delete hotel
    suspend fun deleteHotel(hotelId: String, onResult: (Result<Unit>) -> Unit)

    // Real-time listeners
    fun listenToHotelsByOwner(ownerId: String, onUpdate: (List<HotelModel>) -> Unit)
    fun stopListening()

    // Helper
    fun documentToHotel(document: DocumentSnapshot): HotelModel?
}