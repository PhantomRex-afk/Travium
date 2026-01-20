package com.example.travium.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.R
import com.example.travium.ui.theme.TraviumTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraviumTheme {
                SettingsBody()
            }
        }
    }
}

@Composable
fun SettingsBody() {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = "Back",
                    modifier = Modifier.clickable { /* Handle back */ }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsHeader("Account")
            }
            items(accountSettings) { item ->
                SettingsListItem(item)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsHeader("Preferences")
            }
            items(preferenceSettings) { item ->
                SettingsListItem(item)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsHeader("Support")
            }
            items(supportSettings) { item ->
                SettingsListItem(item)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(
                    onClick = { /* Handle logout */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
}

@Composable
fun SettingsListItem(item: SettingsItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Icon(
            painter = painterResource(android.R.drawable.arrow_down_float), // Placeholder for chevron
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}

data class SettingsItemData(val title: String, val icon: Int)

val accountSettings = listOf(
    SettingsItemData("Edit Profile", R.drawable.profile),
    SettingsItemData("Security", R.drawable.more_buttons),
    SettingsItemData("Privacy", R.drawable.more_buttons)
)

val preferenceSettings = listOf(
    SettingsItemData("Notifications", R.drawable.notification),
    SettingsItemData("Language", R.drawable.more_buttons),
    SettingsItemData("Dark Mode", R.drawable.more_buttons)
)

val supportSettings = listOf(
    SettingsItemData("Help Center", R.drawable.more_buttons),
    SettingsItemData("Terms of Service", R.drawable.more_buttons),
    SettingsItemData("About", R.drawable.more_buttons)
)

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SettingsBody()
}
