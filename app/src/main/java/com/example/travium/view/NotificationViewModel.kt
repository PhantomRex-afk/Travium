package com.example.travium.view

import androidx.lifecycle.ViewModel
import com.example.travium.Model.ReportModel
import com.example.travium.Repository.ReportRepo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("reports")

    private val _reports = MutableStateFlow<List<ReportModel>>(emptyList())
    val reports: StateFlow<List<ReportModel>> = _reports.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        fetchReports()
    }

    private fun fetchReports() {
        _loading.value = true
        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportList = mutableListOf<ReportModel>()
                for (child in snapshot.children) {
                    child.getValue(ReportModel::class.java)?.let {
                        reportList.add(it)
                    }
                }
                _reports.value = reportList.reversed() // Latest reports first
                _loading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _loading.value = false
            }
        })
    }
}
