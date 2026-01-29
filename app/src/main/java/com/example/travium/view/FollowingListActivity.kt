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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class FollowingUi(
    val id: String,
    val name: String,
    val username: String,
    val profileImageUrl: String?,
    val lastActive: String? = null,
    val isOnline: Boolean = false,
    val mutualFriends: Int = 0,
    val isLoaded: Boolean = true
)

class FollowingListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FollowingListBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingListBody() {
    val context = LocalContext.current
    val userRepository = remember { UserRepoImpl() }
    val auth = FirebaseAuth.getInstance()
    val listState = rememberLazyListState()

    // Premium color palette
    val darkNavy = Color(0xFF000033)
    val primaryBlue = Color(0xFF0EA5E9)
    val secondaryBlue = Color(0xFF38BDF8)
    val accentBlue = Color(0xFF7DD3FC)
    val bgColor = darkNavy
    val cardBg = Color(0xFF1E293B)
    val bubbleColor = primaryBlue.copy(alpha = 0.1f)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF94A3B8)
    val textTertiary = Color(0xFF64748B)

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<FollowingUi?>(null) }

    // Get intent extras
    val activity = context as? ComponentActivity
    val userId = activity?.intent?.getStringExtra("USER_ID") ?: ""

    // Current user info
    val currentUserId = auth.currentUser?.uid ?: ""
    val isOwnProfile = currentUserId == userId

    // State for following
    var followingDetails by remember { mutableStateOf<List<FollowingUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load following
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            loadFollowing(userId, userRepository) { following ->
                followingDetails = following.filter { it.id != userId } // Exclude self from the list
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    val filteredFollowing = remember(searchQuery, followingDetails) {
        if (searchQuery.isEmpty()) {
            followingDetails
        } else {
            followingDetails.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.username.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            ChatLikeTopAppBar(
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchToggle = { isSearching = !isSearching },
                onClearSearch = { searchQuery = "" },
                onBackClick = { (context as? ComponentActivity)?.finish() },
                title = "Following (${filteredFollowing.size})"
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    ChatLoadingState(primaryBlue = primaryBlue)
                }
                filteredFollowing.isEmpty() && searchQuery.isNotEmpty() -> {
                    ChatEmptySearchState()
                }
                filteredFollowing.isEmpty() -> {
                    ChatEmptyFollowingState()
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(filteredFollowing) { index, following ->
                            FollowingChatItem(
                                following = following,
                                index = index,
                                primaryBlue = primaryBlue,
                                cardBg = cardBg,
                                bubbleColor = bubbleColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                textTertiary = textTertiary,
                                onUnfollow = {
                                    if (isOwnProfile && currentUserId.isNotEmpty()) {
                                        userRepository.unfollowUser(
                                            currentUserId = currentUserId,
                                            targetUserId = following.id
                                        ) { success, message ->
                                            if (success) {
                                                followingDetails = followingDetails.filter { it.id != following.id }
                                                Toast.makeText(
                                                    context,
                                                    "Unfollowed ${following.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to unfollow: $message",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isOwnProfile) "Please login to unfollow" else "Can only unfollow your own following",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                context = context,
                                isOwnProfile = isOwnProfile,
                                onClick = {
                                    val intent = Intent(context, ProfileActivity::class.java)
                                    intent.putExtra("USER_ID", following.id)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatLikeTopAppBar(
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onClearSearch: () -> Unit,
    onBackClick: () -> Unit,
    title: String
) {
    CenterAlignedTopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearching,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                label = "titleAnimation"
            ) { searching ->
                if (!searching) {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                } else {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search following...",
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
                                IconButton(onClick = onClearSearch) {
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
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    if (isSearching) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearching) "Close Search" else "Search",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF000033)
        )
    )
}

@Composable
fun FollowingChatItem(
    following: FollowingUi,
    index: Int,
    primaryBlue: Color,
    cardBg: Color,
    bubbleColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    onUnfollow: () -> Unit,
    context: Context,
    isOwnProfile: Boolean,
    onClick: () -> Unit
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
            initialOffsetY = { it / 2 }
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
                // Online indicator and avatar
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = primaryBlue.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (following.profileImageUrl != null) {
                            AsyncImage(
                                model = following.profileImageUrl,
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
                                    text = following.name.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryBlue
                                )
                            }
                        }
                    }

                    // Online indicator
                    if (following.isOnline) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .size(12.dp)
                                .padding(2.dp)
                                .shadow(2.dp, CircleShape)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = following.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        if (following.mutualFriends > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bubbleColor,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "${following.mutualFriends} mutual",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = following.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        maxLines = 1
                    )

                    // Last active
                    following.lastActive?.let { lastActive ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Active $lastActive",
                            style = MaterialTheme.typography.labelSmall,
                            color = textTertiary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Unfollow button/menu
                if (isOwnProfile) {
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
                                text = {
                                    Text(
                                        "Unfollow",
                                        color = Color(0xFFEF4444)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onUnfollow()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatLoadingState(primaryBlue: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = primaryBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = primaryBlue,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Loading following...",
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
fun ChatEmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.LightGray.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.LightGray
                )
            }
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
fun ChatEmptyFollowingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.LightGray.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.LightGray
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No following yet",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Follow people to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

// Helper function to load following (updated to include more data)
private fun loadFollowing(
    userId: String,
    userRepository: UserRepoImpl,
    onComplete: (List<FollowingUi>) -> Unit
) {
    val followingRef = FirebaseDatabase.getInstance().getReference("following").child(userId)

    followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val following = mutableListOf<FollowingUi>()
            var loadedCount = 0
            val totalCount = snapshot.childrenCount.toInt()

            if (totalCount == 0) {
                onComplete(emptyList())
                return
            }

            for (followingSnapshot in snapshot.children) {
                val followingId = followingSnapshot.key ?: continue

                userRepository.getUserById(followingId) { user ->
                    if (user != null) {
                        val followingUi = FollowingUi(
                            id = user.userId,
                            name = user.fullName.ifEmpty { "User" },
                            username = user.username.ifEmpty { "@user" },
                            profileImageUrl = if (user.profileImageUrl.isNotEmpty()) user.profileImageUrl else null,
                            lastActive = "recently",
                            isOnline = false,
                            mutualFriends = 0
                        )

                        following.add(followingUi)
                        loadedCount++
                        if (loadedCount == totalCount) {
                            onComplete(following.sortedBy { it.name })
                        }
                    } else {
                        // Add placeholder
                        following.add(FollowingUi(
                            id = followingId,
                            name = "User",
                            username = "@user",
                            profileImageUrl = null,
                            lastActive = null,
                            isOnline = false,
                            mutualFriends = 0
                        ))
                        loadedCount++
                        if (loadedCount == totalCount) {
                            onComplete(following.sortedBy { it.name })
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