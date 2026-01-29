package com.example.travium.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.travium.ui.theme.TraviumTheme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("USER_ID")
        setContent {
            TraviumTheme {
                if (userId != null) {
                    ProfileScreen(userId = userId)
                } else {
                    // Handle error: No user ID provided
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileActivityPreview() {
    TraviumTheme {
        ProfileScreen(userId = "preview")
    }
}
