package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travium.Model.ReportModel
import com.example.travium.Repository.ReportRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportViewModel(private val repository: ReportRepo) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _submissionStatus = MutableStateFlow<String?>(null)
    val submissionStatus: StateFlow<String?> = _submissionStatus.asStateFlow()

    fun submitReport(
        reportedUserId: String,
        reportedByUserId: String,
        reason: String,
        details: String
    ) {
        val report = ReportModel(
            reportedUserId = reportedUserId,
            reportedByUserId = reportedByUserId,
            reason = reason,
            additionalDetails = details
        )

        _loading.value = true
        repository.submitReport(report) { success, msg ->
            _loading.value = false
            _submissionStatus.value = msg
        }
    }

    fun clearStatus() {
        _submissionStatus.value = null
    }
}