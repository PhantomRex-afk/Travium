package com.example.travium.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.travium.model.ChatListItemModel
import com.example.travium.repository.ChatRepo
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatRepo: ChatRepo,
    currentUserId: String,
    onChatClick: (ChatListItemModel) -> Unit
) {
    var chatList by remember { mutableStateOf(listOf<ChatListItemModel>()) }

    // Load chat list from repo
    LaunchedEffect(currentUserId) {
        chatRepo.getChatList(currentUserId) { list ->
            chatList = list.sortedByDescending { it.timestamp }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chats") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(chatList) { chat ->
                ChatListItemView(chat) { onChatClick(chat) }
            }
        }
    }
}

@Composable
fun ChatListItemView(chat: ChatListItemModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            // Display first letter of userId for now
            Text(
                text = chat.userId.first().uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.userName.ifEmpty { chat.userId },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        chat.timestamp?.let {
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
    Divider()
}
