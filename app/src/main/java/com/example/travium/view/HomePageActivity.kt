package com.example.travium.view

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.R
import com.example.travium.utils.ImageUtils

class HomePageActivity : ComponentActivity() {

    lateinit var imageUtils: ImageUtils
    var selectedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            selectedImageUri = uri
        }
        setContent {
            MainScreen(
                selectedImageUri = selectedImageUri,
                onPickImage = { imageUtils.launchImagePicker() }
            )
        }
    }
}

// Dummy data for notifications
data class Notification(val id: Int, val user: String, val message: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
) {

    data class NavItems(val label : String, val icon: Int)
    var selectedIndex by remember { mutableStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }

    // Start with an empty list to simulate not being logged in
    val notifications = remember { emptyList<Notification>() }

    val listItems = listOf(
        NavItems("Home", R.drawable.outline_home_24),
        NavItems("Guide", R.drawable.outline_map_pin_review_24),
        NavItems("Post", R.drawable.addbox),
        NavItems("ChatBox", icon = R.drawable.chatbox),
        NavItems("Profile", R.drawable.profile),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Travium", style = TextStyle(
                                fontSize = 35.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.LightGray
                    ),
                    actions = {
                        IconButton(onClick = { showNotifications = !showNotifications }) {
                            Icon(
                                painter = painterResource(R.drawable.notification),
                                contentDescription = "Notifications"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp)) // Adjust spacing
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    listItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(painter = painterResource(item.icon),
                                    contentDescription = item.label)
                            },
                            label = {Text(item.label)},
                            selected = selectedIndex == index,
                            onClick = {selectedIndex=index}
                        )
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when(selectedIndex){
                        0 -> HomeScreenBody()
                        1 -> HomeScreenBody()
                        2 -> MakePostBody(selectedImageUri = selectedImageUri, onPickImage = onPickImage)
                        3 -> Text("Chat Feature Coming Soon!") // Placeholder to prevent crash
                        else -> HomeScreenBody()
                    }
                }
            }
        }

        // Notification Panel Overlay
        AnimatedVisibility(
            visible = showNotifications,
            enter = slideInVertically(animationSpec = tween(durationMillis = 300)) { fullHeight -> -fullHeight },
            exit = slideOutVertically(animationSpec = tween(durationMillis = 300)) { fullHeight -> -fullHeight }
        ) {
            NotificationPanel(notifications = notifications)
        }
    }
}

@Composable
fun NotificationPanel(notifications: List<Notification>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp), // Adjust this padding to position below the TopAppBar
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No new notifications")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.profile), // Replace with user profile pic
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "${notification.user} ${notification.message}")
    }
}


@Preview(showBackground = true)
@Composable
fun HomePreviewer(){
    MainScreen(selectedImageUri = null, onPickImage = {})
}
