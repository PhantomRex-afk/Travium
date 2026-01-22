package com.example.travium.repository

import com.example.travium.model.NotificationModel
import com.google.firebase.database.FirebaseDatabase

class AdminnotificationRepoImpl : AdminNotificationRepo {
    private val database = FirebaseDatabase.getInstance().getReference("admin_notifications")

    override fun sendNotification(notification: NotificationModel, callback: (Boolean, String?) -> Unit) {
        val notificationId = database.push().key ?: return callback(false, "Failed to generate ID")
        val finalNotification = notification.copy(
            notificationId = notificationId,
            timestamp = System.currentTimeMillis()
        )
        
        database.child(notificationId).setValue(finalNotification)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Notification sent successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to send notification")
                }
            }
    }

    override fun getAllNotifications(callback: (List<NotificationModel>?, String?) -> Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val notifications = mutableListOf<NotificationModel>()
            snapshot.children.forEach { child ->
                child.getValue(NotificationModel::class.java)?.let {
                    notifications.add(it)
                }
            }
            callback(notifications, null)
        }.addOnFailureListener {
            callback(null, it.message ?: "Failed to fetch notifications")
        }
    }

    override fun deleteNotification(notificationId: String, callback: (Boolean, String?) -> Unit) {
        database.child(notificationId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Notification deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete notification")
                }
            }
    }
}
