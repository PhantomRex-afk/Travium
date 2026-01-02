package com.example.travium.Repository

import com.example.travium.Model.ReportModel

interface ReportRepo {
    fun submitReport(
        report: ReportModel,
        callback: (Boolean, String) -> Unit
    )
}
