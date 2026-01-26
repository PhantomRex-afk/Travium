package com.example.travium.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
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
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = language)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                    modifier = Modifier.clickable { (context as? Activity)?.finish() }
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
                SettingsListItem(item) {
                    if (item.title == "Edit Profile") {
                        context.startActivity(Intent(context, EditProfileActivity::class.java))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsHeader("Security & Privacy")
            }
            items(securitySettings) { item ->
                SettingsListItem(item) {
                    if (item.title == "Change Password") {
                        context.startActivity(Intent(context, ChangePasswordActivity::class.java))
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsHeader("Preferences")
            }
            items(preferenceSettings) { item ->
                SettingsListItem(
                    item = item,
                    value = if (item.title == "Language") selectedLanguage else null
                ) { 
                    if (item.title == "Language") {
                        showLanguageDialog = true
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsHeader("Support")
            }
            items(supportSettings) { item ->
                SettingsListItem(item) { /* Handle click */ }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
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
fun SettingsListItem(item: SettingsItemData, value: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        if (value != null) {
            Text(
                text = value,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
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
