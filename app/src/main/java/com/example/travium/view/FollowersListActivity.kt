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
import com.example.travium.model.UserModel
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
    val isLoaded: Boolean = true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersListBody() {
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
    val followButtonColor = Color(0xFF10B981)
    val unfollowButtonColor = Color(0xFFEF4444)

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
            followerDetails.filter {
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
                                "Followers",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            FollowersSearchBar(
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
                                "Loading followers...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
                filteredFollowers.isEmpty() && searchQuery.isNotEmpty() -> {
                    FollowersEmptySearchState()
                }
                filteredFollowers.isEmpty() -> {
                    FollowersEmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(filteredFollowers) { index, follower ->
                            FollowerCard(
                                follower = follower,
                                index = index,
                                primaryBlue = primaryBlue,
                                secondaryBlue = secondaryBlue,
                                accentBlue = accentBlue,
                                cardBg = cardBg,
                                followButtonColor = followButtonColor,
                                unfollowButtonColor = unfollowButtonColor,
                                onFollowToggle = {
                                    if (currentUserId.isNotEmpty()) {
                                        if (follower.isFollowingBack) {
                                            // Unfollow
                                            userRepository.unfollowUser(
                                                currentUserId = currentUserId,
                                                targetUserId = follower.id
                                            ) { success, message ->
                                                if (success) {
                                                    val updatedList = followerDetails.toMutableList()
                                                    val indexToUpdate = updatedList.indexOfFirst { it.id == follower.id }
                                                    if (indexToUpdate != -1) {
                                                        updatedList[indexToUpdate] = updatedList[indexToUpdate].copy(
                                                            isFollowingBack = false
                                                        )
                                                        followerDetails = updatedList
                                                        Toast.makeText(
                                                            context,
                                                            "Unfollowed ${follower.name}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to unfollow: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            // Follow
                                            userRepository.followUser(
                                                currentUserId = currentUserId,
                                                targetUserId = follower.id
                                            ) { success, message ->
                                                if (success) {
                                                    val updatedList = followerDetails.toMutableList()
                                                    val indexToUpdate = updatedList.indexOfFirst { it.id == follower.id }
                                                    if (indexToUpdate != -1) {
                                                        updatedList[indexToUpdate] = updatedList[indexToUpdate].copy(
                                                            isFollowingBack = true
                                                        )
                                                        followerDetails = updatedList
                                                        Toast.makeText(
                                                            context,
                                                            "Followed ${follower.name}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to follow: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please login to follow",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                context = context
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to load followers
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
            var loadedCount = 0
            val totalCount = snapshot.childrenCount.toInt()

            if (totalCount == 0) {
                onComplete(emptyList())
                return
            }

            for (followerSnapshot in snapshot.children) {
                val followerId = followerSnapshot.key ?: continue

                userRepository.getUserById(followerId) { user ->
                    if (user != null) {
                        val followerUi = FollowerUi(
                            id = user.userId,
                            name = user.fullName.ifEmpty { "User" },
                            username = user.username.ifEmpty { "@user" },
                            profileImageUrl = if (user.profileImageUrl.isNotEmpty()) user.profileImageUrl else null,
                            isFollowingBack = false
                        )

                        // Check if current user is following this follower
                        if (currentUserId.isNotEmpty()) {
                            userRepository.isFollowing(currentUserId, followerId) { isFollowing ->
                                followerUi.isFollowingBack = isFollowing
                                followers.add(followerUi)
                                loadedCount++
                                if (loadedCount == totalCount) {
                                    onComplete(followers.sortedBy { it.name })
                                }
                            }
                        } else {
                            followers.add(followerUi)
                            loadedCount++
                            if (loadedCount == totalCount) {
                                onComplete(followers.sortedBy { it.name })
                            }
                        }
                    } else {
                        // Add placeholder
                        followers.add(FollowerUi(
                            id = followerId,
                            name = "User",
                            username = "@user",
                            profileImageUrl = null,
                            isFollowingBack = false
                        ))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search followers...", color = Color.White.copy(alpha = 0.7f)) },
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
fun FollowersEmptySearchState() {
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
            "No followers found",
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
fun FollowersEmptyState() {
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
            "No followers yet",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Text(
            "When someone follows this profile, they'll appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun FollowerCard(
    follower: FollowerUi,
    index: Int,
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    cardBg: Color,
    followButtonColor: Color,
    unfollowButtonColor: Color,
    onFollowToggle: () -> Unit,
    context: Context
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
                        name = follower.name,
                        profileImageUrl = follower.profileImageUrl
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Follower info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = follower.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = follower.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Follow/Unfollow button (only show if not current user)
                    if (context as? ComponentActivity? != null) {
                        Button(
                            onClick = { onFollowToggle() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (follower.isFollowingBack) unfollowButtonColor else followButtonColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (follower.isFollowingBack) "Following" else "Follow")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAvatar(
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    name: String,
    profileImageUrl: String?
) {
    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accentBlue,
                            secondaryBlue,
                            primaryBlue,
                            accentBlue
                        )
                    )
                )
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (!profileImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryBlue, secondaryBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (name.isNotEmpty()) name.first().toString().uppercase() else "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}