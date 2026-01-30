package com.example.travium.view

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.Comment
import com.example.travium.model.MakePostModel
import com.example.travium.model.UserModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Travel-themed dark colors
val TravelDeepNavy = Color(0xFF0F172A)
val TravelCardNavy = Color(0xFF1E293B)
val TravelAccentTeal = Color(0xFF2DD4BF)
val TravelSoftGray = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenBody() {
    val postViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val allPosts by postViewModel.allPosts.observeAsState(initial = emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        postViewModel.getAllPosts()
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            val usersRef = FirebaseDatabase.getInstance().getReference("users")
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = mutableListOf<UserModel>()
                    val lowercasedQuery = searchQuery.lowercase()
                    for (child in snapshot.children) {
                        val user = child.getValue(UserModel::class.java)
                        if (user != null && (user.username.lowercase().contains(lowercasedQuery) || user.fullName.lowercase().contains(lowercasedQuery))) {
                            users.add(user)
                        }
                    }
                    searchResults = users.sortedWith(compareBy({ !it.username.lowercase().startsWith(lowercasedQuery) && !it.fullName.lowercase().startsWith(lowercasedQuery) }, { it.username }))
                    isSearching = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isSearching = false
                }
            })
        } else {
            searchResults = emptyList()
            isSearching = false
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(TravelDeepNavy)
    ) {
        Column {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search for users...", color = TravelSoftGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TravelAccentTeal) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TravelSoftGray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TravelAccentTeal,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            if (searchQuery.isNotBlank()) {
                when {
                    isSearching -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TravelAccentTeal)
                        }
                    }
                    searchResults.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No users found.", color = TravelSoftGray, fontSize = 16.sp)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(searchResults) { user ->
                                UserSearchItem(user = user) {
                                    val intent = Intent(context, ProfileActivity::class.java)
                                    intent.putExtra("USER_ID", user.userId)
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            } else {
                // Feed
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(allPosts) { post ->
                        PostCard(
                            post = post, 
                            postViewModel = postViewModel, 
                            userViewModel = userViewModel,
                            onCommentClick = {
                                selectedPostId = post.postId
                                isSheetOpen = true
                            }
                        )
                    }
                }
            }
        }

        if (isSheetOpen && selectedPostId != null) {
            val latestPost = allPosts.find { it.postId == selectedPostId }
            
            if (latestPost != null) {
                ModalBottomSheet(
                    onDismissRequest = { isSheetOpen = false },
                    sheetState = sheetState,
                    containerColor = TravelCardNavy,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = TravelSoftGray) },
                    modifier = Modifier.fillMaxHeight(0.9f)
                ) {
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

@Composable
fun UserSearchItem(user: UserModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(TravelCardNavy),
            contentAlignment = Alignment.Center
        ) {
            if (user.profileImageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(user.profileImageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (if (user.username.isNotEmpty()) user.username else user.fullName).take(1).uppercase(),
                    color = TravelAccentTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = if (user.username.isNotEmpty()) "@${user.username}" else user.fullName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun PostAuthorHeader(userId: String, userViewModel: UserViewModel) {
    var user by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(userId) {
        userViewModel.getUserById(userId) { fetchedUser ->
            user = fetchedUser
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Placeholder for user avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(TravelAccentTeal, Color(0xFF3B82F6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (user?.profileImageUrl?.isNotEmpty() == true) {
                Image(
                    painter = rememberAsyncImagePainter(user?.profileImageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (user?.username?.take(1) ?: user?.fullName?.take(1) ?: "T").uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = user?.username?.let { "@$it" } ?: user?.fullName ?: "Traveler",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun PostCard(
    post: MakePostModel, 
    postViewModel: MakePostViewModel, 
    userViewModel: UserViewModel,
    onCommentClick: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardNavy)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            PostAuthorHeader(userId = post.userId, userViewModel = userViewModel)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (post.caption.isNotEmpty()) {
                Text(
                    text = post.caption,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            
            if (post.location.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TravelAccentTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.location,
                        color = TravelSoftGray,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (post.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (currentUserId.isNotEmpty()) {
                        postViewModel.likePost(post.postId, currentUserId) { success ->
                            if (!success) {
                                Toast.makeText(context, "Failed to like post", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Login to interact", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "Like",
                        tint = if (post.likes.contains(currentUserId)) Color.Red else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${post.likes.size}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(onClick = onCommentClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.comment),
                        contentDescription = "Comment",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${post.comments.size}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CommentSection(post: MakePostModel, postViewModel: MakePostViewModel, userViewModel: UserViewModel) {
    var newCommentText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var currentUserProfile by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId) { user ->
                currentUserProfile = user
            }
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .imePadding()
    ) {
        Text(
            "Comments",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(post.comments) { comment ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (if (comment.fullName.startsWith("@")) comment.fullName.drop(1) else comment.fullName).take(1).uppercase(),
                            color = TravelAccentTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (comment.fullName.startsWith("@")) comment.fullName else "@${comment.fullName}",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = comment.message,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Share your thoughts...", color = TravelSoftGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TravelAccentTeal,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    if (currentUserId.isNotEmpty() && newCommentText.isNotBlank()) {
                        val comment = Comment(
                            userId = currentUserId,
                            fullName = currentUserProfile?.username ?: currentUserProfile?.fullName ?: "Explorer",
                            message = newCommentText
                        )
                        postViewModel.addComment(post.postId, comment) { success ->
                            if (success) {
                                newCommentText = ""
                            }
                        }
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = TravelAccentTeal)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreviewer(){
    HomeScreenBody()
}
