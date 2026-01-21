package com.example.travium.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSupportBody() {

    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Support", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {

                item {
                    Text(
                        text = "Need Help?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Contact our support team and we’ll get back to you as soon as possible.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                item {
                    Text(
                        text = "Subject",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your issue subject") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Message",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = {
                            Text(
                                "Describe your problem in detail...",
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 10,
                        singleLine = false
                    )
                }
            }

            Button(
                onClick = {
                    if (subject.isNotBlank() && message.isNotBlank()) {
                        showConfirmation = true
                        subject = ""
                        message = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = subject.isNotBlank() && message.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            confirmButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("OK")
                }
            },
            title = { Text("Request Sent") },
            text = {
                Text(
                    "Thank you for contacting Travium support. Our team will respond shortly."
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerSupportPreview() {
    CustomerSupportBody()
}
