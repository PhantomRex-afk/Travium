package com.example.travium.ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travium.model.ChatMessageModel
import com.example.travium.repository.ChatRepo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRepo: ChatRepo,
    chatId: String,
    currentUserId: String,
    otherUserId: String,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<ChatMessageModel>()) }
    var newMessage by remember { mutableStateOf("") }

    // Load messages
    LaunchedEffect(chatId) {
        chatRepo.getMessages(chatId) { list ->
            messages = list.sortedBy { it.timestamp }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 4.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    MessageItem(
                        message = message,
                        isCurrentUser = message.senderId == currentUserId
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                                .padding(8.dp)
                        ) { inner() }
                    }
                )
                Button(onClick = {
                    if (newMessage.isNotBlank()) {
                        chatRepo.sendMessage(chatId, currentUserId, otherUserId, newMessage)
                        newMessage = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}


@Composable
fun MessageItem(message: ChatMessageModel, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            // Avatar for receiver
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            ) {
                Text(
                    text = message.senderId.first().uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message bubble
        Column {
            Surface(
                color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier.padding(12.dp),
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Timestamp
            Text(
                text = message.timestamp?.let { java.text.SimpleDateFormat("hh:mm a").format(it) } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
            )
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar for sender
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ) {
                Text(
                    text = message.senderId.first().uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

