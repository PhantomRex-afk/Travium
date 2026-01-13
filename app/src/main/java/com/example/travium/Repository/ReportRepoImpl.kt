package com.example.travium.repository

import com.example.travium.model.ReportModel
import com.google.firebase.database.FirebaseDatabase

class ReportRepoImpl : ReportRepo {
    private val database = FirebaseDatabase.getInstance().getReference("reports")

    override fun submitReport(
        report: ReportModel,
        callback: (Boolean, String) -> Unit
    ) {
        val reportId = database.push().key ?: return callback(false, "Failed to generate report ID")
        val finalReport = report.copy(reportId = reportId)
        
        database.child(reportId).setValue(finalReport)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Report submitted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Submission failed")
                }
            }
    }
}
