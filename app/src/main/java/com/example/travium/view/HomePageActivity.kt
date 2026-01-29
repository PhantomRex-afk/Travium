package com.example.travium.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travium.R
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.ui.theme.*
import com.example.travium.utils.ImageUtils
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class HomePageActivity : ComponentActivity() {

    private lateinit var imageUtils: ImageUtils
    private var selectedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            selectedImageUri = uri
        }

        setContent {
            HomePageScreen(
                selectedImageUri = selectedImageUri,
                onPickImage = { imageUtils.launchImagePicker() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageScreen(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
) {
    val context = LocalContext.current

    val userViewModel: UserViewModel = viewModel {
        UserViewModel(UserRepoImpl())
    }
    val postViewModel: MakePostViewModel = viewModel {
        MakePostViewModel(MakePostRepoImpl())
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUser by userViewModel.userData.observeAsState()

    var selectedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId) {}
            postViewModel.getAllPosts()
        }
    }

    Scaffold(
        containerColor = TravelDeepNavy,
        bottomBar = {
            NavigationBar(containerColor = TravelCardNavy) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            if (index == 4 && currentUser?.isGuide == true) {
                                // Navigate to GuideProfileScreen Activity
                                context.startActivity(
                                    Intent(context, GuideProfileScreen::class.java)
                                        .putExtra("GUIDE_ID", currentUserId)
                                )
                            } else {
                                selectedIndex = index
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TravelAccentTeal,
                            selectedTextColor = TravelAccentTeal,
                            unselectedIconColor = TravelSoftGray,
                            unselectedTextColor = TravelSoftGray
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedIndex) {
                0 -> HomeScreenBody()
                1 -> HomeScreenBody()
                2 -> MakePostBody(selectedImageUri, onPickImage)
                3 -> ChatScreen()
                4 -> ProfileScreen(userId = currentUserId)
                else -> Unit
            }
        }
    }
}

/* ------------------ NAV ITEMS ------------------ */

data class NavItems(
    val label: String,
    val icon: Int
)

val navItems = listOf(
    NavItems("Home", R.drawable.outline_home_24),
    NavItems("Guide", R.drawable.outline_map_pin_review_24),
    NavItems("Post", R.drawable.addbox),
    NavItems("Chat", R.drawable.chatbox),
    NavItems("Profile", R.drawable.profile),
)
