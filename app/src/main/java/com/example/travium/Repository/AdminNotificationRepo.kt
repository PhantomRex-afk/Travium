package com.example.travium.repository

import com.example.travium.model.NotificationModel

interface AdminNotificationRepo {
    fun sendNotification(notification: NotificationModel, callback: (Boolean, String?) -> Unit)
    fun getAllNotifications(callback: (List<NotificationModel>?, String?) -> Unit)
    fun deleteNotification(notificationId: String, callback: (Boolean, String?) -> Unit)
}
