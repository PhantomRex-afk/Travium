package com.example.travium.repository

import android.util.Log
import com.example.travium.model.HotelModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HotelRepoImpl : HotelRepo {
    private val db = Firebase.firestore
    private val hotelsCollection = db.collection(HotelModel.COLLECTION_NAME)

    private var listener: ListenerRegistration? = null

    override suspend fun createHotel(hotel: HotelModel, onResult: (Result<HotelModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hotelRef = hotelsCollection.document(hotel.hotelId)
                hotelRef.set(hotel).await()

                withContext(Dispatchers.Main) {
                    onResult(Result.success(hotel))
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error creating hotel: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getHotelById(hotelId: String, onResult: (Result<HotelModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val document = hotelsCollection.document(hotelId).get().await()
                if (document.exists()) {
                    val hotel = documentToHotel(document)
                    withContext(Dispatchers.Main) {
                        onResult(Result.success(hotel!!))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(Result.failure(Exception("Hotel not found")))
                    }
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error getting hotel: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getAllHotels(onResult: (Result<List<HotelModel>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = hotelsCollection
                    .whereEqualTo("isActive", true)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val hotels = querySnapshot.documents.mapNotNull { documentToHotel(it) }
                withContext(Dispatchers.Main) {
                    onResult(Result.success(hotels))
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error getting all hotels: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getHotelsByOwner(ownerId: String, onResult: (Result<List<HotelModel>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = hotelsCollection
                    .whereEqualTo("hotelOwnerId", ownerId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val hotels = querySnapshot.documents.mapNotNull { documentToHotel(it) }
                withContext(Dispatchers.Main) {
                    onResult(Result.success(hotels))
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error getting owner hotels: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun searchHotels(
        city: String?,
        country: String?,
        minPrice: Double?,
        maxPrice: Double?,
        guests: Int?,
        onResult: (Result<List<HotelModel>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var query: Query = hotelsCollection.whereEqualTo("isActive", true)

                city?.let {
                    query = query.whereEqualTo("city", city)
                }

                country?.let {
                    query = query.whereEqualTo("country", country)
                }

                val querySnapshot = query
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                // Filter by price and capacity in memory
                val filteredHotels = querySnapshot.documents.mapNotNull { documentToHotel(it) }
                    .filter { hotel ->
                        var matches = true

                        guests?.let {
                            matches = matches && hotel.roomTypes.any { roomType -> roomType.maxGuests >= guests }
                        }

                        minPrice?.let {
                            matches = matches && hotel.roomTypes.minOfOrNull { roomType -> roomType.pricePerNight } ?: 0.0 >= minPrice
                        }

                        maxPrice?.let {
                            matches = matches && hotel.roomTypes.maxOfOrNull { roomType -> roomType.pricePerNight } ?: Double.MAX_VALUE <= maxPrice
                        }

                        matches
                    }

                withContext(Dispatchers.Main) {
                    onResult(Result.success(filteredHotels))
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error searching hotels: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun updateHotel(hotelId: String, hotel: HotelModel, onResult: (Result<Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                hotelsCollection.document(hotelId)
                    .set(hotel, SetOptions.merge())
                    .await()

                withContext(Dispatchers.Main) {
                    onResult(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error updating hotel: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun updateRoomAvailability(
        hotelId: String,
        roomTypeId: String,
        bookedRooms: Int,
        onResult: (Result<Unit>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getHotelById(hotelId) { result ->
                    result.onSuccess { hotel ->
                        val updatedRoomTypes = hotel.roomTypes.map { roomType ->
                            if (roomType.typeId == roomTypeId) {
                                roomType.copy(availableRooms = roomType.availableRooms - bookedRooms)
                            } else {
                                roomType
                            }
                        }

                        val updatedHotel = hotel.copy(roomTypes = updatedRoomTypes)

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                hotelsCollection.document(hotelId)
                                    .update("roomTypes", updatedRoomTypes)
                                    .await()

                                withContext(Dispatchers.Main) {
                                    onResult(Result.success(Unit))
                                }
                            } catch (e: Exception) {
                                Log.e("HotelRepoImpl", "Error updating room availability: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    onResult(Result.failure(e))
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error in updateRoomAvailability: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun deleteHotel(hotelId: String, onResult: (Result<Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                hotelsCollection.document(hotelId).delete().await()
                withContext(Dispatchers.Main) {
                    onResult(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("HotelRepoImpl", "Error deleting hotel: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override fun listenToHotelsByOwner(ownerId: String, onUpdate: (List<HotelModel>) -> Unit) {
        stopListening()

        listener = hotelsCollection
            .whereEqualTo("hotelOwnerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HotelRepoImpl", "Listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                val hotels = snapshot?.documents?.mapNotNull { documentToHotel(it) } ?: emptyList()
                onUpdate(hotels)
            }
    }

    override fun stopListening() {
        listener?.remove()
        listener = null
    }

    override fun documentToHotel(document: DocumentSnapshot): HotelModel? {
        return try {
            document.toObject(HotelModel::class.java)
        } catch (e: Exception) {
            Log.e("HotelRepoImpl", "Error converting document to hotel: ${e.message}", e)
            null
        }
    }
}