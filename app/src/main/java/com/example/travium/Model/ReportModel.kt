package com.example.travium.model

data class ReportModel(
    val reportId: String = "",
    val reportedUserId: String = "",
    val reportedByUserId: String = "",
    val reason: String = "",
    val additionalDetails: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
