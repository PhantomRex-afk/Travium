package com.example.travium.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travium.repository.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

data class FollowerUi(
    val id: String,
    val name: String,
    val username: String,
    val profileImageUrl: String?,
    var isFollowingBack: Boolean = false,
    val lastActive: String? = null,
    val isOnline: Boolean = false
)

class FollowersListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FollowersListBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FollowersListBody() {
    val context = LocalContext.current
    val userRepository = remember { UserRepoImpl() }
    val auth = FirebaseAuth.getInstance()
    val listState = rememberLazyListState()

    // Color palette
    val darkNavy = Color(0xFF000033)
    val primaryBlue = Color(0xFF0EA5E9)
    val bgColor = darkNavy
    val cardBg = Color(0xFF1E293B)
    val bubbleColor = primaryBlue.copy(alpha = 0.1f)
    val followButtonColor = Color(0xFF10B981)
    val unfollowButtonColor = Color(0xFFEF4444)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF94A3B8)
    val textTertiary = Color(0xFF64748B)

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Get intent extras
    val activity = context as? ComponentActivity
    val userId = activity?.intent?.getStringExtra("USER_ID") ?: ""

    // Current user info
    val currentUserId = auth.currentUser?.uid ?: ""

    // State for followers
    var followerDetails by remember { mutableStateOf<List<FollowerUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load followers
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            loadFollowers(userId, currentUserId, userRepository) { followers ->
                followerDetails = followers
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    val filteredFollowers = remember(searchQuery, followerDetails) {
        if (searchQuery.isEmpty()) {
            followerDetails
        } else {
            followerDetails.filter { follower ->
                follower.name.contains(searchQuery, ignoreCase = true) ||
                        follower.username.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (!isSearching) {
                        Text(
                            "Followers (${followerDetails.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = { newQuery -> searchQuery = newQuery },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Search followers...",
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.White
                            ),
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Close Search" else "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = darkNavy
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingState(primaryBlue = primaryBlue)
                }
                filteredFollowers.isEmpty() && searchQuery.isNotEmpty() -> {
                    EmptySearchState()
                }
                filteredFollowers.isEmpty() -> {
                    EmptyFollowersState()
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(filteredFollowers) { index, follower ->
                            FollowerItem(
                                follower = follower,
                                index = index,
                                primaryBlue = primaryBlue,
                                cardBg = cardBg,
                                followButtonColor = followButtonColor,
                                unfollowButtonColor = unfollowButtonColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                textTertiary = textTertiary,
                                onFollowToggle = {
                                    if (currentUserId.isNotEmpty()) {
                                        val newFollowState = !follower.isFollowingBack
                                        val action = if (newFollowState) userRepository::followUser else userRepository::unfollowUser
                                        action(currentUserId, follower.id) { success, message ->
                                            if (success) {
                                                val updatedList = followerDetails.map {
                                                    if (it.id == follower.id) it.copy(isFollowingBack = newFollowState) else it
                                                }
                                                followerDetails = updatedList
                                                val toastMessage = if(newFollowState) "Followed ${follower.name}" else "Unfollowed ${follower.name}"
                                                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Action failed: $message", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Please login to follow", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                context = context,
                                onClick = {
                                    val intent = Intent(context, ProfileActivity::class.java)
                                    intent.putExtra("USER_ID", follower.id)
                                    context.startActivity(intent)
                                },
                                isCurrentUserItem = currentUserId == follower.id
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FollowerItem(
    follower: FollowerUi,
    index: Int,
    primaryBlue: Color,
    cardBg: Color,
    followButtonColor: Color,
    unfollowButtonColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    onFollowToggle: () -> Unit,
    context: Context,
    onClick: () -> Unit,
    isCurrentUserItem: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { fullHeight -> (fullHeight / 2) }
        ) + fadeIn(animationSpec = tween(300))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            color = cardBg,
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        shape = CircleShape,
                        color = primaryBlue.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (follower.profileImageUrl != null) {
                            AsyncImage(
                                model = follower.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = follower.name.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryBlue
                                )
                            }
                        }
                    }

                    if (follower.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = follower.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = follower.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        maxLines = 1
                    )

                    if (follower.lastActive != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Active ${follower.lastActive}",
                            style = MaterialTheme.typography.labelSmall,
                            color = textTertiary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (!isCurrentUserItem) {
                    if (follower.isFollowingBack) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = textTertiary
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Unfollow", color = unfollowButtonColor) },
                                    onClick = {
                                        showMenu = false
                                        onFollowToggle()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = unfollowButtonColor) }
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onFollowToggle,
                            colors = ButtonDefaults.buttonColors(containerColor = followButtonColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Follow", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState(primaryBlue: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(primaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = primaryBlue,
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Loading followers...",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Fetching your connections",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No users found",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Try different search terms",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun EmptyFollowersState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No followers yet",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "When someone follows this profile, they'll appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

private fun loadFollowers(
    userId: String,
    currentUserId: String,
    userRepository: UserRepoImpl,
    onComplete: (List<FollowerUi>) -> Unit
) {
    val followersRef = FirebaseDatabase.getInstance().getReference("followers").child(userId)

    followersRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val followers = mutableListOf<FollowerUi>()
            val totalCount = snapshot.childrenCount.toInt()
            if (totalCount == 0) {
                onComplete(emptyList())
                return
            }
            var loadedCount = 0

            for (followerSnapshot in snapshot.children) {
                val followerId = followerSnapshot.key ?: continue
                userRepository.getUserById(followerId) { user ->
                    if (user != null) {
                        userRepository.isFollowing(currentUserId, followerId) { isFollowing ->
                            followers.add(FollowerUi(
                                id = user.userId,
                                name = user.fullName.ifEmpty { "User" },
                                username = user.username.ifEmpty { "@user" },
                                profileImageUrl = if (user.profileImageUrl.isNotEmpty()) user.profileImageUrl else null,
                                isFollowingBack = isFollowing,
                                lastActive = "recently",
                                isOnline = (System.currentTimeMillis() % 2 == 0L) // Mock online status
                            ))
                            loadedCount++
                            if (loadedCount == totalCount) {
                                onComplete(followers.sortedBy { it.name })
                            }
                        }
                    } else {
                        loadedCount++
                        if (loadedCount == totalCount) {
                            onComplete(followers.sortedBy { it.name })
                        }
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            onComplete(emptyList())
        }
    })
}