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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.repository.GroupChatRepoImpl
import com.example.travium.model.UserModel
import com.example.travium.repository.ChatRepositoryImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.utils.ChatItem
import com.example.travium.viewmodel.ChatViewModel
import com.example.travium.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Get current user info
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

    // ViewModels
    val chatViewModel = ChatViewModel(
        chatRepository = ChatRepositoryImpl(),
        groupChatRepository = GroupChatRepoImpl()
    )

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    // State
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Private, 2: Groups

    // Load data
    val allChats by chatViewModel.allChats.observeAsState(initial = emptyList())
    val loading by chatViewModel.loading.observeAsState(initial = false)

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            chatViewModel.loadAllChats(currentUserId)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && currentUserId.isNotEmpty()) {
            userViewModel.searchUsers(searchQuery) { users: List<UserModel> ->
                searchResults = users.filter { it.userId != currentUserId }
            }
        } else {
            searchResults = emptyList()
        }
    }

    // Filter chats for tabs
    val filteredChats = if (searchQuery.isBlank()) {
        allChats
    } else {
        allChats.filter { chatItem ->
            when (chatItem) {
                is ChatItem.Private -> {
                    val otherName = if (chatItem.chatRoom.participant1Id == currentUserId) {
                        chatItem.chatRoom.participant2Name
                    } else {
                        chatItem.chatRoom.participant1Name
                    }
                    otherName.contains(searchQuery, ignoreCase = true) ||
                            chatItem.chatRoom.lastMessage.contains(searchQuery, ignoreCase = true)
                }
                is ChatItem.Group -> {
                    chatItem.groupChat.groupName.contains(searchQuery, ignoreCase = true) ||
                            chatItem.groupChat.lastMessage.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val privateChats = filteredChats.filterIsInstance<ChatItem.Private>()
    val groupChats = filteredChats.filterIsInstance<ChatItem.Group>()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to CreateGroupChat screen
                    val intent = Intent(context, CreateGroupChatActivity::class.java).apply {
                        putExtra("currentUserId", currentUserId)
                        putExtra("currentUserName", currentUserName)
                    }
                    context.startActivity(intent)
                },
                containerColor = TravelAccentTeal,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = "Create Group",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    placeholder = { Text("Search followers/following...", color = TravelSoftGray) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TravelAccentTeal
                        )
                    },
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

                if (searchQuery.isNotEmpty()) {
                    // Show search results (followers/following)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        items(searchResults) { user ->
                            UserSearchItem(
                                user = user,
                                currentUserId = currentUserId,
                                currentUserName = currentUserName,
                                chatViewModel = chatViewModel
                            )
                        }
                    }
                } else {
                    // Show chat list with tabs
                    Column {
                        // Tab Row
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = TravelDeepNavy,
                            contentColor = TravelAccentTeal,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    height = 2.dp,
                                    color = TravelAccentTeal
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        "All Chats",
                                        color = if (selectedTab == 0) Color.White else TravelSoftGray
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        "Private",
                                        color = if (selectedTab == 1) Color.White else TravelSoftGray
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Text(
                                        "Groups",
                                        color = if (selectedTab == 2) Color.White else TravelSoftGray
                                    )
                                }
                            )
                        }

                        // Chat List
                        when {
                            loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = TravelAccentTeal)
                                }
                            }
                            filteredChats.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "No Chats",
                                            modifier = Modifier.size(64.dp),
                                            tint = TravelSoftGray
                                        )
                                        Text(
                                            text = when (selectedTab) {
                                                1 -> "No private chats yet"
                                                2 -> "No group chats yet"
                                                else -> "No chats yet. Start a conversation!"
                                            },
                                            color = TravelSoftGray,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                            else -> {
                                val itemsToShow = when (selectedTab) {
                                    0 -> filteredChats // All
                                    1 -> privateChats
                                    2 -> groupChats
                                    else -> filteredChats
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(
                                        items = itemsToShow,
                                        key = { item ->
                                            when (item) {
                                                is ChatItem.Private -> "private_${item.chatRoom.chatId}"
                                                is ChatItem.Group -> "group_${item.groupChat.groupId}"
                                            }
                                        }
                                    ) { chatItem ->
                                        ChatListItem(
                                            chatItem = chatItem,
                                            currentUserId = currentUserId,
                                            onClick = {
                                                when (chatItem) {
                                                    is ChatItem.Private -> {
                                                        val otherParticipantId =
                                                            if (chatItem.chatRoom.participant1Id == currentUserId) {
                                                                chatItem.chatRoom.participant2Id
                                                            } else {
                                                                chatItem.chatRoom.participant1Id
                                                            }

                                                        val otherParticipantName =
                                                            if (chatItem.chatRoom.participant1Id == currentUserId) {
                                                                chatItem.chatRoom.participant2Name
                                                            } else {
                                                                chatItem.chatRoom.participant1Name
                                                            }

                                                        val otherParticipantImage =
                                                            if (chatItem.chatRoom.participant1Id == currentUserId) {
                                                                chatItem.chatRoom.participant2Photo
                                                            } else {
                                                                chatItem.chatRoom.participant1Photo
                                                            }

                                                        // Navigate to individual chat
                                                        val intent = Intent(
                                                            context,
                                                            ChatActivity::class.java
                                                        ).apply {
                                                            putExtra("receiverId", otherParticipantId)
                                                            putExtra("receiverName", otherParticipantName)
                                                            putExtra("receiverImage", otherParticipantImage)
                                                            putExtra("currentUserId", currentUserId)
                                                            putExtra("currentUserName", currentUserName)
                                                        }
                                                        context.startActivity(intent)
                                                    }
                                                    is ChatItem.Group -> {
                                                        // Navigate to group chat activity
                                                        scope.launch {
                                                            Toast.makeText(
                                                                context,
                                                                "Opening group: ${chatItem.groupChat.groupName}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchItem(
    user: UserModel,
    currentUserId: String,
    currentUserName: String,
    chatViewModel: ChatViewModel
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Create or get chat room and navigate to chat
                chatViewModel.getOrCreateChatRoom(
                    participant1Id = currentUserId,
                    participant2Id = user.userId,
                    participant1Name = currentUserName,
                    participant2Name = user.fullName.ifEmpty { user.username },
                    participant1Photo = "",
                    participant2Photo = user.profileImageUrl
                ) { chatRoom ->
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        putExtra("receiverId", user.userId)
                        putExtra("receiverName", user.fullName.ifEmpty { user.username })
                        putExtra("receiverImage", user.profileImageUrl)
                        putExtra("currentUserId", currentUserId)
                        putExtra("currentUserName", currentUserName)
                    }
                    context.startActivity(intent)
                }
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
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
                    text = (user.username.ifEmpty { user.fullName }).take(1).uppercase(),
                    color = TravelAccentTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = user.fullName.ifEmpty { user.username },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (user.username.isNotEmpty()) {
                Text(
                    text = "@${user.username}",
                    color = TravelSoftGray,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = "Message",
            tint = TravelAccentTeal,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ChatListItem(
    chatItem: ChatItem,
    currentUserId: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardNavy)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Icon/Image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TravelCardNavy),
                contentAlignment = Alignment.Center
            ) {
                when (chatItem) {
                    is ChatItem.Private -> {
                        val otherParticipantImage =
                            if (chatItem.chatRoom.participant1Id == currentUserId) {
                                chatItem.chatRoom.participant2Photo
                            } else {
                                chatItem.chatRoom.participant1Photo
                            }

                        if (otherParticipantImage.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(otherParticipantImage),
                                contentDescription = "User",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "User",
                                modifier = Modifier.fillMaxSize(),
                                tint = TravelAccentTeal
                            )
                        }
                    }
                    is ChatItem.Group -> {
                        if (chatItem.groupChat.groupImage.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(chatItem.groupChat.groupImage),
                                contentDescription = chatItem.groupChat.groupName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "Group",
                                modifier = Modifier.fillMaxSize(),
                                tint = TravelAccentTeal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chat Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (chatItem) {
                            is ChatItem.Private -> {
                                if (chatItem.chatRoom.participant1Id == currentUserId) {
                                    chatItem.chatRoom.participant2Name
                                } else {
                                    chatItem.chatRoom.participant1Name
                                }
                            }
                            is ChatItem.Group -> chatItem.groupChat.groupName
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatChatTime(chatItem.lastMessageTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = TravelSoftGray
                    )
                }

                Text(
                    text = chatItem.lastMessage.ifEmpty {
                        when (chatItem) {
                            is ChatItem.Private -> "Start a conversation"
                            is ChatItem.Group -> "No messages yet"
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TravelSoftGray,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // Unread badge
                val unreadCount = when (chatItem) {
                    is ChatItem.Group -> chatItem.groupChat.unreadCount
                    is ChatItem.Private -> 0 // Add unreadCount to ChatRoom model if needed
                }

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TravelAccentTeal)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else "$unreadCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatChatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val currentDate = Date()
    val diff = currentDate.time - date.time

    return when {
        diff < 60 * 60 * 1000 -> {
            // Less than 1 hour
            val minutes = (diff / (60 * 1000)).toInt()
            "${minutes}m ago"
        }
        diff < 24 * 60 * 60 * 1000 -> {
            // Less than 1 day
            val hours = (diff / (60 * 60 * 1000)).toInt()
            "${hours}h ago"
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            // Less than 1 week
            val days = (diff / (24 * 60 * 60 * 1000)).toInt()
            "${days}d ago"
        }
        else -> {
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    }
}