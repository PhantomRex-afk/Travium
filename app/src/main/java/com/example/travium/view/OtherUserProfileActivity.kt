package com.example.travium.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.MakePostModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.ui.theme.TraviumTheme
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class OtherUserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getStringExtra("userId") ?: ""
        setContent {
            TraviumTheme {
                OtherUserProfileScreen(userId, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileScreen(userId: String, onBack: () -> Unit) {
    val postViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var userPosts by remember { mutableStateOf<List<MakePostModel>>(emptyList()) }

    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val midnightBlue = Color(0xFF003366)
    val darkNavy = Color(0xFF000033)
    val cyanAccent = Color(0xFF00FFFF)

    LaunchedEffect(userId) {
        if (userId.isEmpty()) return@LaunchedEffect
        val database = Firebase.database
        
        // Fetch User Details
        val userRef = database.getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Anonymous"
                username = snapshot.child("username").getValue(String::class.java) ?: ""
                bio = snapshot.child("bio").getValue(String::class.java) ?: "No bio yet"
                profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch User Posts
        val postsRef = database.getReference("posts")
        postsRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<MakePostModel>()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(MakePostModel::class.java)
                        if (post != null) posts.add(post)
                    }
                    userPosts = posts.reversed()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    Box(modifier = Modifier.fillMaxSize().background(darkNavy)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /* Header Section with integrated Design */
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                    
                    // Profile Image with ring
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    Brush.sweepGradient(listOf(cyanAccent, Color.Blue, cyanAccent)),
                                    CircleShape
                                )
                        )
                        Surface(
                            shape = CircleShape,
                            color = darkNavy,
                            modifier = Modifier.size(104.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    profileImageUrl ?: R.drawable.profile
                                ),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username handle
                    Text(
                        if (username.isNotEmpty()) "@$username" else fullName,
                        color = cyanAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp
                    )
                    
                    Text(
                        bio,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pill-Style Stats Layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(userPosts.size.toString(), "POSTS", cyanAccent)
                        VerticalDivider(modifier = Modifier.height(20.dp), thickness = 1.dp, color = Color.White.copy(alpha = 0.1f))
                        StatItem("0", "FOLLOWERS", cyanAccent)
                        VerticalDivider(modifier = Modifier.height(20.dp), thickness = 1.dp, color = Color.White.copy(alpha = 0.1f))
                        StatItem("0", "FOLLOWING", cyanAccent)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cyanAccent,
                                contentColor = darkNavy
                            )
                        ) {
                            Text("FOLLOW", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { 
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, cyanAccent),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = cyanAccent
                            )
                        ) {
                            Text("MESSAGE", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            /* Gallery Grid */
            items(userPosts) { post ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { 
                            selectedPostId = post.postId
                            isSheetOpen = true
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(post.imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Count overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = post.likes.size.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isSheetOpen && selectedPostId != null) {
            val latestPost = userPosts.find { it.postId == selectedPostId }
            
            if (latestPost != null) {
                ModalBottomSheet(
                    onDismissRequest = { isSheetOpen = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFF1E293B),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxHeight(0.9f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        PostCard(
                            post = latestPost,
                            postViewModel = postViewModel,
                            userViewModel = userViewModel,
                            onCommentClick = {}
                        )
                        CommentSection(
                            post = latestPost,
                            postViewModel = postViewModel,
                            userViewModel = userViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(label, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
