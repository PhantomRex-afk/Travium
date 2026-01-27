package com.example.travium.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travium.R
import com.example.travium.repository.ReportRepoImpl
import com.example.travium.view.ui.theme.TraviumTheme
import com.example.travium.viewmodel.ReportViewModel

class ReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Assume these are passed via Intent
        val reportedUserId = intent.getStringExtra("REPORTED_USER_ID") ?: ""
        val reportedByUserId = intent.getStringExtra("REPORTED_BY_USER_ID") ?: ""
        val targetName = intent.getStringExtra("TARGET_NAME") ?: "this traveller"

        setContent {
            TraviumTheme {
                // Using viewModel builder to instantiate ReportViewModel without a separate Factory class
                val viewModel: ReportViewModel = viewModel {
                    ReportViewModel(ReportRepoImpl())
                }
                ReportBody(
                    reportedUserId = reportedUserId,
                    reportedByUserId = reportedByUserId,
                    targetName = targetName,
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBody(
    reportedUserId: String = "",
    reportedByUserId: String = "",
    targetName: String = "this traveller",
    viewModel: ReportViewModel? = null,
    onBack: () -> Unit = {}
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var additionalDetails by remember { mutableStateOf("") }
    val context = LocalContext.current

    val isLoading by viewModel?.loading?.collectAsState() ?: remember { mutableStateOf(false) }
    val submissionStatus by viewModel?.submissionStatus?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    LaunchedEffect(submissionStatus) {
        submissionStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("success", ignoreCase = true)) {
                onBack()
            }
            viewModel?.clearStatus()
        }
    }

    val reportReasons = listOf(
        "Suspicious travel behavior",
        "Fake travel itinerary",
        "Inappropriate behavior in group chat",
        "Unreliable co-traveller (No-show)",
        "Safety concern / Harassment",
        "Spam or misleading travel post",
        "Scam or financial fraud",
        "Impersonation",
        "Something else"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Activity", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Report $targetName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        Text(
                            text = "Help us maintain a safe community for solo travellers. Your report helps identify suspicious behavior and unreliable group members.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }

                    items(reportReasons) { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reason,
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp,
                                color = if (selectedReason == reason) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = (selectedReason == reason),
                                onClick = { selectedReason = reason }
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Additional Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Please provide more context or specific details about the issue.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = additionalDetails,
                            onValueChange = { additionalDetails = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { 
                                Text(
                                    "Describe the incident, specific dates, or why you find this behavior suspicious...",
                                    fontSize = 14.sp
                                ) 
                            },
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 10,
                            singleLine = false
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                Button(
                    onClick = { 
                        if (selectedReason != null) {
                            viewModel?.submitReport(
                                reportedUserId = reportedUserId,
                                reportedByUserId = reportedByUserId,
                                reason = selectedReason!!,
                                details = additionalDetails
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedReason != null && !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Submit Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportPreview() {
    ReportBody(targetName = "Blastoise")
}
