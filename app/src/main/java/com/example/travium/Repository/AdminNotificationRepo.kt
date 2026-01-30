package com.example.travium.repository

import com.example.travium.model.AdminNotificationModel

interface AdminNotificationRepo {
    fun sendNotification(notification: AdminNotificationModel, callback: (Boolean, String?) -> Unit)
    fun getAllNotifications(callback: (List<AdminNotificationModel>?, String?) -> Unit)
    fun deleteNotification(notificationId: String, callback: (Boolean, String?) -> Unit)
}
