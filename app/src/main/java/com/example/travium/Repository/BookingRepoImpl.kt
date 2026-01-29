package com.example.travium.repository

import android.util.Log
import com.example.travium.model.BookingModel
import com.example.travium.model.BookingStatus
import com.example.travium.model.PaymentStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookingRepoImpl : BookingRepo {
    private val db = Firebase.firestore
    private val bookingsCollection = db.collection(BookingModel.COLLECTION_NAME)
    private val auth = FirebaseAuth.getInstance()

    private var listener: ListenerRegistration? = null

    override suspend fun createBooking(booking: BookingModel, onResult: (Result<BookingModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bookingRef = bookingsCollection.document(booking.bookingId)
                bookingRef.set(booking).await()

                // Send notification to hotel owner
                sendBookingNotification(booking)

                withContext(Dispatchers.Main) {
                    onResult(Result.success(booking))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error creating booking: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getBookingById(bookingId: String, onResult: (Result<BookingModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val document = bookingsCollection.document(bookingId).get().await()
                if (document.exists()) {
                    val booking = documentToBooking(document)
                    withContext(Dispatchers.Main) {
                        onResult(Result.success(booking!!))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(Result.failure(Exception("Booking not found")))
                    }
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error getting booking: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getUserBookings(userId: String, onResult: (Result<List<BookingModel>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = bookingsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("bookingDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val bookings = querySnapshot.documents.mapNotNull { documentToBooking(it) }
                withContext(Dispatchers.Main) {
                    onResult(Result.success(bookings))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error getting user bookings: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getHotelBookings(hotelId: String, onResult: (Result<List<BookingModel>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = bookingsCollection
                    .whereEqualTo("hotelId", hotelId)
                    .orderBy("bookingDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val bookings = querySnapshot.documents.mapNotNull { documentToBooking(it) }
                withContext(Dispatchers.Main) {
                    onResult(Result.success(bookings))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error getting hotel bookings: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getHotelOwnerBookings(hotelOwnerId: String, onResult: (Result<List<BookingModel>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = bookingsCollection
                    .whereEqualTo("hotelOwnerId", hotelOwnerId)
                    .orderBy("bookingDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val bookings = querySnapshot.documents.mapNotNull { documentToBooking(it) }
                withContext(Dispatchers.Main) {
                    onResult(Result.success(bookings))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error getting hotel owner bookings: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun updateBookingStatus(
        bookingId: String,
        status: BookingStatus,
        onResult: (Result<Unit>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "bookingStatus" to status.name
                )

                bookingsCollection.document(bookingId)
                    .update(updates)
                    .await()

                // Send status update notification
                getBookingById(bookingId) { result ->
                    result.onSuccess { booking ->
                        sendStatusUpdateNotification(booking, status)
                    }
                }

                withContext(Dispatchers.Main) {
                    onResult(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error updating booking status: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun updatePaymentStatus(
        bookingId: String,
        status: PaymentStatus,
        onResult: (Result<Unit>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "paymentStatus" to status.name
                )

                bookingsCollection.document(bookingId)
                    .update(updates)
                    .await()

                withContext(Dispatchers.Main) {
                    onResult(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error updating payment status: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun updateBooking(
        bookingId: String,
        booking: BookingModel,
        onResult: (Result<Unit>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                bookingsCollection.document(bookingId)
                    .set(booking, SetOptions.merge())
                    .await()

                withContext(Dispatchers.Main) {
                    onResult(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error updating booking: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override suspend fun cancelBooking(bookingId: String, onResult: (Result<Unit>) -> Unit) {
        updateBookingStatus(bookingId, BookingStatus.CANCELLED, onResult)
    }

    override suspend fun deleteBooking(bookingId: String, onResult: (Result<Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                bookingsCollection.document(bookingId).delete().await()
                withContext(Dispatchers.Main) {
                    onResult(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("BookingRepoImpl", "Error deleting booking: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(Result.failure(e))
                }
            }
        }
    }

    override fun listenToUserBookings(userId: String, onUpdate: (List<BookingModel>) -> Unit) {
        stopListening()

        listener = bookingsCollection
            .whereEqualTo("userId", userId)
            .orderBy("bookingDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookingRepoImpl", "Listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                val bookings = snapshot?.documents?.mapNotNull { documentToBooking(it) } ?: emptyList()
                onUpdate(bookings)
            }
    }

    override fun listenToHotelBookings(hotelId: String, onUpdate: (List<BookingModel>) -> Unit) {
        stopListening()

        listener = bookingsCollection
            .whereEqualTo("hotelId", hotelId)
            .orderBy("bookingDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookingRepoImpl", "Listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                val bookings = snapshot?.documents?.mapNotNull { documentToBooking(it) } ?: emptyList()
                onUpdate(bookings)
            }
    }

    override fun stopListening() {
        listener?.remove()
        listener = null
    }

    override fun documentToBooking(document: DocumentSnapshot): BookingModel? {
        return try {
            document.toObject(BookingModel::class.java)
        } catch (e: Exception) {
            Log.e("BookingRepoImpl", "Error converting document to booking: ${e.message}", e)
            null
        }
    }

    private fun sendBookingNotification(booking: BookingModel) {
        // In a real app, you would use FCM (Firebase Cloud Messaging) to send push notifications
        // For now, we'll log it and you can implement FCM later
        Log.d("BookingNotification",
            """
            New Booking Notification for Hotel Owner:
            Hotel: ${booking.hotelName}
            Guest: ${booking.userName}
            Check-in: ${booking.checkInDate}
            Check-out: ${booking.checkOutDate}
            Rooms: ${booking.numberOfRooms} (${booking.roomType})
            Total: $${booking.totalPrice}
            Status: ${booking.bookingStatus}
            """
        )

        // You can add FCM notification code here
        // Example:
        // sendPushNotification(
        //     toUserId = booking.hotelOwnerId,
        //     title = "New Booking",
        //     message = "${booking.userName} booked ${booking.hotelName}"
        // )
    }

    private fun sendStatusUpdateNotification(booking: BookingModel, newStatus: BookingStatus) {
        Log.d("BookingNotification",
            """
            Booking Status Update:
            Booking ID: ${booking.bookingId}
            Hotel: ${booking.hotelName}
            Guest: ${booking.userName}
            New Status: $newStatus
            """
        )

        // Send notification to user about status change
        // Example:
        // sendPushNotification(
        //     toUserId = booking.userId,
        //     title = "Booking Status Updated",
        //     message = "Your booking at ${booking.hotelName} is now $newStatus"
        // )
    }

    // Add this method for FCM integration later
    private fun sendPushNotification(toUserId: String, title: String, message: String) {
        // Implement FCM push notification logic here
        // You'll need to:
        // 1. Get the user's FCM token from Firestore
        // 2. Send notification via Firebase Cloud Messaging
        // 3. Handle notification in your app
    }
}