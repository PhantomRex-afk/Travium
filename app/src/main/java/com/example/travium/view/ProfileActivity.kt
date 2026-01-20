package com.example.travium.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.travium.R
import com.example.travium.ui.theme.TraviumTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraviumTheme {
                ProfileBody()
            }
        }
    }
}

@Composable
fun ProfileBody() {
    val context = LocalContext.current
    // Database State
    var userName by remember { mutableStateOf("Loading...") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<String?>(null) }
    var eventCount by remember { mutableIntStateOf(10) }

    val darkNavy = Color(0xFF000033)
    val midnightBlue = Color(0xFF003366)
    val cyanAccent = Color(0xFF00FFFF)

    // Fetch user data from Firebase
    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val database = Firebase.database
            val userRef = database.getReference("users").child(user.uid)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userName = snapshot.child("name").getValue(String::class.java) ?: "User"
                    bio = snapshot.child("bio").getValue(String::class.java) ?: "No bio yet"
                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(containerColor = darkNavy) { padding ->
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
                        contentDescription = "Back",
                        tint = Color.White
                    )
                    Icon(
                        painter = painterResource(R.drawable.more_buttons),
                        contentDescription = "More",
                        tint = Color.White
                    )
                }
            }

            /* Profile Info */
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(2.dp, cyanAccent),
                        color = Color.Transparent,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                imageUri ?: profileImageUrl ?: R.drawable.blastoise
                            ),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .padding(4.dp)
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
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(userName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text(bio, color = Color.White.copy(alpha = 0.7f))
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
                    ProfileStat("714", "Posts", cyanAccent)
                    ProfileStat("10B", "Followers", cyanAccent)
                    ProfileStat("0", "Following", cyanAccent)
                }
            }

            /* Buttons */
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileButton(
                        text = "Edit Profile",
                        modifier = Modifier.weight(1f),
                        cyanAccent,
                        darkNavy,
                        onClick = {
                            context.startActivity(Intent(context, EditProfileActivity::class.java))
                        }
                    )
                    ProfileButton(text = "Message", modifier = Modifier.weight(1f), cyanAccent, darkNavy)
                }
            }

            /* Events Grid Items */
            items(count = eventCount) { index ->
                val title = "Event ${index + 1}"
                StoryCard(
                    imageRes = R.drawable.blastoise,
                    title = title,
                    containerColor = midnightBlue
                ) {
                    selectedEvent = title
                    showDialog = true
                }
            }
        }

        /* Dialog */
        if (showDialog && selectedEvent != null) {
            ProfileEventDetailPopup(
                eventTitle = selectedEvent!!,
                containerColor = midnightBlue,
                accentColor = cyanAccent,
                onDismiss = { showDialog = false }
            )
        }
    }
}

/* ---------- Reusable Components ---------- */

@Composable
fun ProfileStat(value: String, label: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
fun ProfileButton(text: String, modifier: Modifier = Modifier, accentColor: Color, darkNavy: Color, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accentColor,
            contentColor = darkNavy
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StoryCard(
    imageRes: Int,
    title: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ProfileEventDetailPopup(
    eventTitle: String,
    containerColor: Color,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Details for $eventTitle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = containerColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    TraviumTheme {
        ProfileBody()
    }
}
