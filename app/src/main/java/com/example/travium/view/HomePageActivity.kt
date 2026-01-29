package com.example.travium.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travium.R
import com.example.travium.model.NotificationModel
import com.example.travium.model.UserModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.ui.theme.TravelAccentTeal
import com.example.travium.ui.theme.TravelCardNavy
import com.example.travium.ui.theme.TravelDeepNavy
import com.example.travium.ui.theme.TravelSoftGray
import com.example.travium.utils.ImageUtils
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class HomePageActivity : ComponentActivity() {

    lateinit var imageUtils: ImageUtils
    var selectedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
) {
    val context = LocalContext.current
    val postViewModel: MakePostViewModel = viewModel { MakePostViewModel(MakePostRepoImpl()) }
    val userViewModel: UserViewModel = viewModel { UserViewModel(UserRepoImpl()) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    val notifications by postViewModel.notifications.observeAsState(initial = emptyList())
    val currentUser by userViewModel.userData.observeAsState()
    
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }
    var hasUnreadNotifications by remember { mutableStateOf(false) }
    
    var lastNotificationCount by remember { mutableIntStateOf(-1) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            postViewModel.getNotifications(currentUserId)
            userViewModel.getUserById(currentUserId) { }
        }
    }

    // Dynamic Navigation Logic: If the user is a guide, redirect to GuideProfileScreen when clicking Profile
    LaunchedEffect(selectedIndex, currentUser) {
        if (selectedIndex == 4 && currentUser?.isGuide == true) {
            val intent = Intent(context, GuideProfileScreen::class.java)
            intent.putExtra("GUIDE_ID", currentUserId)
            context.startActivity(intent)
            // Revert selection to Home so user isn't stuck on an empty profile tab if they come back
            selectedIndex = 0 
        }
    }

    LaunchedEffect(notifications) {
        if (lastNotificationCount != -1 && notifications.size > lastNotificationCount && !showNotifications) {
            hasUnreadNotifications = true
        }
        lastNotificationCount = notifications.size
    }

    LaunchedEffect(showNotifications) {
        if (showNotifications) {
            hasUnreadNotifications = false
        }
    }

    data class NavItems(val label : String, val icon: Int)
    val listItems = listOf(
        NavItems("Home", R.drawable.outline_home_24),
        NavItems("Guide", R.drawable.outline_map_pin_review_24),
        NavItems("Post", R.drawable.addbox),
        NavItems("ChatBox", icon = R.drawable.chatbox),
        NavItems("Profile", R.drawable.profile),
    )

    Box(modifier = Modifier.fillMaxSize().background(TravelDeepNavy)) {
        Scaffold(
            containerColor = TravelDeepNavy,
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Travium", style = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = TravelCardNavy
                        ),
                        actions = {
                            IconButton(onClick = { showNotifications = !showNotifications }) {
                                BadgedBox(
                                    badge = {
                                        if (hasUnreadNotifications) {
                                            Badge(containerColor = TravelAccentTeal)
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.notification),
                                        contentDescription = "Notifications",
                                        tint = Color.White
                                    )
                                }
                            }
                            IconButton(onClick = { 
                                context.startActivity(Intent(context, SettingsActivity::class.java))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Settings",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                    NavigationBar(
                        containerColor = TravelCardNavy,
                        tonalElevation = 8.dp
                    ) {
                        listItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(painter = painterResource(item.icon),
                                        contentDescription = item.label)
                                },
                                label = { Text(item.label) },
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TravelAccentTeal,
                                    selectedTextColor = TravelAccentTeal,
                                    unselectedIconColor = TravelSoftGray,
                                    unselectedTextColor = TravelSoftGray,
                                    indicatorColor = TravelAccentTeal.copy(alpha = 0.1f)
                                )
                            )
                        }
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
                        3 -> ChatScreen()
                        4 -> {
                            if (currentUser == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = TravelAccentTeal)
                                }
                            } else {
                                ProfileScreen(userId = currentUserId)
                            }
                        }
                        else -> HomeScreenBody()
                    }
                }
            }
        }

        if (showNotifications) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showNotifications = false
                    }
            )
        }

        AnimatedVisibility(
            visible = showNotifications,
            enter = slideInVertically(animationSpec = tween(durationMillis = 300)) { fullHeight -> -fullHeight },
            exit = slideOutVertically(animationSpec = tween(durationMillis = 300)) { fullHeight -> -fullHeight }
        ) {
            NotificationPanel(notifications = notifications, userViewModel = userViewModel)
        }
    }
}

@Composable
fun NotificationPanel(notifications: List<NotificationModel>, userViewModel: UserViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(top = 64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }, 
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardNavy),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No new notifications", color = TravelSoftGray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(notifications) { notification ->
                    NotificationItem(notification, userViewModel)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationModel, userViewModel: UserViewModel) {
    var fromUser by remember { mutableStateOf<UserModel?>(null) }
    
    LaunchedEffect(notification.fromUserId) {
        userViewModel.getUserById(notification.fromUserId) { user ->
            fromUser = user
        }
    }

    val message = when(notification.type) {
        "like" -> "liked your post."
        "comment" -> "commented: \"${notification.message}\""
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TravelDeepNavy),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fromUser?.fullName?.take(1)?.uppercase() ?: "T",
                color = TravelAccentTeal,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "${fromUser?.fullName ?: "Someone"} $message",
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreviewer(){
    MainScreen(selectedImageUri = null, onPickImage = {})
}
