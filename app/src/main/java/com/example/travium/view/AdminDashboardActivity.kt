package com.example.travium.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.view.ui.theme.TraviumTheme

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraviumTheme {
                AdminDashboardScreen(
                    onNavigateToNotifications = {
                        startActivity(Intent(this, AdminNotificationActivity::class.java))
                    },
                    onNavigateToReports = {
                        // Assuming NotificationActivity is where you view User Reports
                        startActivity(Intent(this, NotificationActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToNotifications: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Management Tools", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            
            AdminCard(
                title = "User Reports",
                subtitle = "View and manage user activity reports",
                icon = Icons.Default.Warning,
                color = Color(0xFFFF5252),
                onClick = onNavigateToReports
            )

            AdminCard(
                title = "System Notifications",
                subtitle = "Send global alerts to all users",
                icon = Icons.Default.Notifications,
                color = Color(0xFF448AFF),
                onClick = onNavigateToNotifications
            )

            AdminCard(
                title = "Support Statistics",
                subtitle = "Monitor customer support trends",
                icon = Icons.Default.Info,
                color = Color(0xFF4CAF50),
                onClick = { /* Future Stats feature */ }
            )
        }
    }
}

@Composable
fun AdminCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
