package com.example.travium.view

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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

data class FollowingUi(
    val id: String,
    val name: String,
    val username: String,
    val profileImageUrl: String?,
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

    // Premium color palette
    val primaryBlue = Color(0xFF0EA5E9)
    val secondaryBlue = Color(0xFF38BDF8)
    val accentBlue = Color(0xFF7DD3FC)
    val bgGradientStart = Color(0xFFF0F9FF)
    val bgGradientEnd = Color(0xFFE0F2FE)
    val cardBg = Color(0xFFFFFFFF)
    val unfollowButtonColor = Color(0xFFEF4444)

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

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
                followingDetails = following
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
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isSearching,
                        transitionSpec = {
                            fadeIn(tween(300)) + slideInVertically() togetherWith
                                    fadeOut(tween(300)) + slideOutVertically()
                        },
                        label = "titleAnimation"
                    ) { searching ->
                        if (!searching) {
                            Text(
                                "Following",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            FollowingSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onClear = { searchQuery = "" }
                            )
                        }
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
                    IconButton(
                        onClick = {
                            isSearching = !isSearching
                            if (!isSearching) searchQuery = ""
                        }
                    ) {
                        Icon(
                            if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Close Search" else "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(bgGradientStart, bgGradientEnd, Color.White),
                        startY = 0f,
                        endY = 1500f
                    )
                )
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = primaryBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading following...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
                filteredFollowing.isEmpty() && searchQuery.isNotEmpty() -> {
                    FollowingEmptySearchState()
                }
                filteredFollowing.isEmpty() -> {
                    FollowingEmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(filteredFollowing) { index, following ->
                            FollowingCard(
                                following = following,
                                index = index,
                                primaryBlue = primaryBlue,
                                secondaryBlue = secondaryBlue,
                                accentBlue = accentBlue,
                                cardBg = cardBg,
                                unfollowButtonColor = unfollowButtonColor,
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
                                isOwnProfile = isOwnProfile
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to load following
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
                            profileImageUrl = if (user.profileImageUrl.isNotEmpty()) user.profileImageUrl else null
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
                            profileImageUrl = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search following...", color = Color.White.copy(alpha = 0.7f)) },
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
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                }
            }
        }
    )
}

@Composable
fun FollowingEmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No users found",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Text(
            "Try searching with different keywords",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun FollowingEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Not following anyone yet",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Text(
            "Start following people to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun FollowingCard(
    following: FollowingUi,
    index: Int,
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    cardBg: Color,
    unfollowButtonColor: Color,
    onUnfollow: () -> Unit,
    context: Context,
    isOwnProfile: Boolean
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 80L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = primaryBlue.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBg
            )
        ) {
            Box {
                // Decorative gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentBlue.copy(alpha = 0.1f),
                                    secondaryBlue.copy(alpha = 0.05f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    UserAvatar(
                        primaryBlue = primaryBlue,
                        secondaryBlue = secondaryBlue,
                        accentBlue = accentBlue,
                        name = following.name,
                        profileImageUrl = following.profileImageUrl
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Following info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = following.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = following.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Unfollow button (only show for own profile)
                    if (isOwnProfile) {
                        Button(
                            onClick = { onUnfollow() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = unfollowButtonColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unfollow")
                        }
                    }
                }
            }
        }
    }
}