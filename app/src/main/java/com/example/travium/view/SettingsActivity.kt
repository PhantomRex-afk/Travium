package com.example.travium.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.R
import com.example.travium.ui.theme.TraviumTheme
import com.google.firebase.auth.FirebaseAuth

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
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    val languages = listOf("English", "Nepali", "Hindi", "Chinese")

    val midnightBlue = Color(0xFF003366)
    val darkNavy = Color(0xFF000033)
    val cyanAccent = Color(0xFF00FFFF)

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = midnightBlue,
            title = { Text("Select Language", color = Color.White) },
            text = {
                Column {
                    languages.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (language == selectedLanguage),
                                onClick = {
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = cyanAccent, unselectedColor = Color.White.copy(alpha = 0.6f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = language, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel", color = cyanAccent)
                }
            }
        )
    }

    Scaffold(
        containerColor = darkNavy,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_left),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                SettingsHeader("Account")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = midnightBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        accountSettings.forEach { item ->
                            SettingsListItem(item) {
                                if (item.title == "Edit Profile") {
                                    context.startActivity(Intent(context, EditProfileActivity::class.java))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsHeader("Security & Privacy")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = midnightBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        securitySettings.forEach { item ->
                            SettingsListItem(item) {
                                if (item.title == "Change Password") {
                                    context.startActivity(Intent(context, ChangePasswordActivity::class.java))
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsHeader("Preferences")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = midnightBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        preferenceSettings.forEach { item ->
                            SettingsListItem(
                                item = item,
                                value = if (item.title == "Language") selectedLanguage else null
                            ) { 
                                if (item.title == "Language") {
                                    showLanguageDialog = true
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsHeader("Support")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = midnightBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        supportSettings.forEach { item ->
                            SettingsListItem(item) { /* Handle click */ }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF4B4B).copy(alpha = 0.1f),
                        contentColor = Color(0xFFFF4B4B)
                    )
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
        color = Color(0xFF00FFFF),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.sp
    )
}

@Composable
fun SettingsListItem(item: SettingsItemData, value: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        if (value != null) {
            Text(
                text = value,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(
            painter = painterResource(android.R.drawable.arrow_down_float), // Placeholder for chevron
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color.White.copy(alpha = 0.3f)
        )
    }
}

data class SettingsItemData(val title: String, val icon: Int)

val accountSettings = listOf(
    SettingsItemData("Edit Profile", R.drawable.profile)
)

val securitySettings = listOf(
    SettingsItemData("Change Password", R.drawable.more_buttons)
)

val preferenceSettings = listOf(
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
