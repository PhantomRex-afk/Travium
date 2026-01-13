package com.example.travium.repository

import com.example.travium.model.ReportModel

interface ReportRepo {
    fun submitReport(
        report: ReportModel,
        callback: (Boolean, String) -> Unit
    )
}
