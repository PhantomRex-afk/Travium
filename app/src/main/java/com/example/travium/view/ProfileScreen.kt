package com.example.travium.view

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userId: String? = null) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    val effectiveUserId = userId ?: currentUserId
    
    val postViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var userPosts by remember { mutableStateOf<List<MakePostModel>>(emptyList()) }

    var followersCount by remember { mutableStateOf(0L) }
    var followingCount by remember { mutableStateOf(0L) }
    var isFollowing by remember { mutableStateOf(false) }
    var currentUserName by remember { mutableStateOf("") }

    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val midnightBlue = Color(0xFF003366)
    val darkNavy = Color(0xFF000033)
    val cyanAccent = Color(0xFF00FFFF)

    LaunchedEffect(effectiveUserId, currentUserId) {
        if (effectiveUserId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(effectiveUserId)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Anonymous"
                    username = snapshot.child("username").getValue(String::class.java) ?: ""
                    bio = snapshot.child("bio").getValue(String::class.java) ?: "No bio yet"
                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            val postsRef = database.getReference("posts")
            postsRef.orderByChild("userId").equalTo(effectiveUserId)
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

            if (currentUserId != null && effectiveUserId != currentUserId) {
                userViewModel.isFollowing(currentUserId, effectiveUserId) { following ->
                    isFollowing = following
                }
            }
            
            if (currentUserId != null) {
                val currentUserRef = database.getReference("users").child(currentUserId)
                currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        currentUserName = snapshot.child("fullName").getValue(String::class.java) ?: ""
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            userViewModel.getFollowersCount(effectiveUserId) { count -> followersCount = count }
            userViewModel.getFollowingCount(effectiveUserId) { count -> followingCount = count }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(darkNavy)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = midnightBlue.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                                        model = profileImageUrl ?: R.drawable.profile
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

                        // Modern Stats Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatBox(userPosts.size.toString(), "POSTS", cyanAccent) { }
                            ProfileStatBox(followersCount.toString(), "FOLLOWERS", cyanAccent) {
                                // Navigate to Followers List logic
                                if (effectiveUserId != null) {
                                    val intent = Intent(context, FollowersListActivity::class.java)
                                    intent.putExtra("USER_ID", effectiveUserId)
                                    context.startActivity(intent)
                                }
                            }
                            ProfileStatBox(followingCount.toString(), "FOLLOWING", cyanAccent) {
                                // Navigate to Following List logic
                                if (effectiveUserId != null) {
                                    val intent = Intent(context, FollowingListActivity::class.java)
                                    intent.putExtra("USER_ID", effectiveUserId)
                                    context.startActivity(intent)
                                }
                            }
                        }

                        if (effectiveUserId != currentUserId && currentUserId != null && effectiveUserId != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isFollowing) {
                                            userViewModel.unfollowUser(currentUserId, effectiveUserId) { success, message ->
                                                if (success) {
                                                    isFollowing = false
                                                }
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            userViewModel.followUser(currentUserId, effectiveUserId) { success, message ->
                                                if (success) {
                                                    isFollowing = true
                                                }
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(isFollowing) midnightBlue else cyanAccent,
                                        contentColor = if(isFollowing) Color.White else darkNavy
                                    )
                                ) {
                                    Text(if(isFollowing) "UNFOLLOW" else "FOLLOW", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { 
                                        val intent = Intent(context, ChatActivity::class.java)
                                        intent.putExtra("receiverId", effectiveUserId)
                                        intent.putExtra("receiverName", fullName)
                                        intent.putExtra("receiverImage", profileImageUrl)
                                        intent.putExtra("currentUserId", currentUserId)
                                        intent.putExtra("currentUserName", currentUserName)
                                        context.startActivity(intent)
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
                        }
                    }
                }
            }

            /* Gallery Grid with Like and Comment Overlays */
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
                        
                        // Glassy count overlay
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
                            onCommentClick = {} // Already in comment view
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
fun ProfileStatBox(value: String, label: String, accentColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(label, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    TraviumTheme {
        ProfileScreen(userId = "preview")
    }
}
