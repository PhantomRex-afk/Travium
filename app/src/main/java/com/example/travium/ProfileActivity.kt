package com.example.travium

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.travium.Model.ProfileModel
import com.example.travium.Repository.ProfileRepoImpl
import com.example.travium.view.ProfileViewModel

@Composable
fun ProfileBody() {
    val context = LocalContext.current
    val repository = remember { ProfileRepoImpl(context) }
    // Using remember for ViewModel to match your existing style
    val viewModel = remember { ProfileViewModel(repository) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<String?>(null) }
    var eventCount by remember { mutableIntStateOf(10) }

    val profile by viewModel.profile.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                imageUri = uri
                // Updated to match the viewModel's uploadImage signature
                viewModel.uploadImage(context, uri) { success, downloadUrl ->
                    if (success && downloadUrl != null) {
                        val currentProfile = profile ?: ProfileModel(
                            username = "Blastoise",
                            category = "Water Type Pokemon",
                            bio = "BigMan Blastoise",
                            subtitle = "Squirtle ko Hajurbau",
                            postsCount = "714",
                            followersCount = "10B",
                            followingCount = "0",
                            events = emptyList()
                        )
                        viewModel.updateProfile(
                            userId = "user_123", // Replace with actual userId
                            model = currentProfile.copy(profileImageUri = downloadUrl)
                        )
                    }
                }
            }
        }
    )

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                /* Top Bar */
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back"
                        )
                        Icon(
                            painter = painterResource(R.drawable.more_buttons),
                            contentDescription = "More"
                        )
                    }
                }

                /* Profile Info */
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                imageUri ?: profile?.profileImageUri ?: R.drawable.blastoise
                            ),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(profile?.username ?: "Blastoise", fontWeight = FontWeight.Bold)
                            Text(profile?.category ?: "Water Type Pokemon")
                            Text(profile?.bio ?: "BigMan Blastoise")
                            Text(profile?.subtitle ?: "Squirtle ko Hajurbau")
                        }
                    }
                }

                /* Stats */
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStat(profile?.postsCount ?: "714", "Posts")
                        ProfileStat(profile?.followersCount ?: "10B", "Followers")
                        ProfileStat(profile?.followingCount ?: "0", "Following")
                    }
                }

                /* Buttons */
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileButton(text = "Hire", modifier = Modifier.weight(1f))
                        ProfileButton(text = "Message", modifier = Modifier.weight(1f))
                    }
                }

                /* Events Grid Items */
                items(count = eventCount) { index ->
                    val title = "Event ${index + 1}"
                    StoryCard(
                        imageRes = R.drawable.blastoise,
                        title = title
                    ) {
                        selectedEvent = title
                        showDialog = true
                    }
                }
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        /* Dialog */
        if (showDialog && selectedEvent != null) {
            ProfileEventDetailPopup(
                eventTitle = selectedEvent!!,
                onDismiss = { showDialog = false }
            )
        }
    }
}

/* ---------- Reusable Components ---------- */

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label)
    }
}

@Composable
fun ProfileButton(text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = {},
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Text(text)
    }
}

@Composable
fun StoryCard(
    imageRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ProfileEventDetailPopup(
    eventTitle: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Details for $eventTitle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    ProfileBody()
}
