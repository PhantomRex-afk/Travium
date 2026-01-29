package com.example.travium.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.travium.model.ChatMessage
import com.example.travium.repository.ChatRepositoryImpl
import com.example.travium.repository.GroupChatRepoImpl
import com.example.travium.viewmodel.ChatViewModel
import com.example.travium.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            ChatRepositoryImpl(),
            groupChatRepository = GroupChatRepoImpl()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val receiverId = intent.getStringExtra("receiverId") ?: ""
        val receiverName = intent.getStringExtra("receiverName") ?: "User"
        val receiverImage = intent.getStringExtra("receiverImage") ?: ""
        val currentUserId = intent.getStringExtra("currentUserId") ?: ""
        val currentUserName = intent.getStringExtra("currentUserName") ?: ""

        chatViewModel.getOrCreateChatRoom(
            participant1Id = currentUserId,
            participant2Id = receiverId,
            participant1Name = currentUserName,
            participant2Name = receiverName,
            participant1Photo = "", // You might want to pass the current user's photo URL here
            participant2Photo = receiverImage
        )

        setContent {
            ChatBody(
                receiverName = receiverName,
                receiverImage = receiverImage,
                receiverId = receiverId,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                chatViewModel = chatViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBody(
    receiverName: String,
    receiverImage: String,
    receiverId: String,
    currentUserId: String,
    currentUserName: String,
    chatViewModel: ChatViewModel
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var messageText by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val chatRoom by chatViewModel.currentChatRoom.observeAsState()
    val messages by chatViewModel.messages.observeAsState(emptyList())
    val typingStatus by chatViewModel.isTyping.observeAsState()
    val loading by chatViewModel.loading.observeAsState(false)
    val error by chatViewModel.error.observeAsState()
    val uploadProgress by chatViewModel.uploadProgress.observeAsState(0.0)
    val isUploading by chatViewModel.isUploading.observeAsState(false)

    val listState = rememberLazyListState()
    var typingJob by remember { mutableStateOf<Job?>(null) }

    val darkNavy = Color(0xFF000033)
    val cyanAccent = Color(0xFF00FFFF)

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            Log.e("ChatActivity", "Error: $it")
            chatViewModel.clearError()
        }
    }

    LaunchedEffect(chatRoom) {
        chatRoom?.chatId?.let {
            chatViewModel.loadMessages(it)
            chatViewModel.listenForNewMessages(it)
            chatViewModel.listenForTypingStatus(it)
            chatViewModel.markMessagesAsRead(it, currentUserId)
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isReceiverTyping = remember(typingStatus, receiverId) {
        typingStatus?.let { (userId, typing) -> userId == receiverId && typing } ?: false
    }

    LaunchedEffect(messageText) {
        typingJob?.cancel()
        chatRoom?.chatId?.let {
            if (messageText.isNotEmpty()) {
                typingJob = coroutineScope.launch {
                    chatViewModel.setTypingStatus(it, currentUserId, true)
                    delay(2000)
                    chatViewModel.setTypingStatus(it, currentUserId, false)
                }
            } else {
                chatViewModel.setTypingStatus(it, currentUserId, false)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            chatRoom?.let { cr ->
                chatViewModel.sendMessage(cr.chatId, currentUserId, receiverId, currentUserName, receiverName, "", "image", it.toString())
            }
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            chatRoom?.let { cr ->
                chatViewModel.sendMessage(cr.chatId, currentUserId, receiverId, currentUserName, receiverName, "", "document", it.toString())
            }
        }
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val localTempUri = tempPhotoUri
        if (success && localTempUri != null) {
            chatRoom?.let { cr ->
                chatViewModel.sendMessage(cr.chatId, currentUserId, receiverId, currentUserName, receiverName, "", "image", localTempUri.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                receiverName = receiverName,
                receiverImage = receiverImage,
                isReceiverTyping = isReceiverTyping,
                onBackClick = { activity?.finish() }
            )
        },
        bottomBar = {
            Column {
                if (showAttachmentDialog) {
                    AttachmentPopup(
                        onDismiss = { showAttachmentDialog = false },
                        onGalleryClick = { imagePickerLauncher.launch("image/*"); showAttachmentDialog = false },
                        onCameraClick = {
                            val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri)
                            showAttachmentDialog = false
                        },
                        onDocumentClick = { documentPickerLauncher.launch("*/*"); showAttachmentDialog = false }
                    )
                }
                MessageInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendClick = {
                        if (messageText.isNotBlank()) {
                            chatRoom?.let { cr ->
                                chatViewModel.sendMessage(cr.chatId, currentUserId, receiverId, currentUserName, receiverName, messageText)
                                messageText = ""
                            }
                        }
                    },
                    onAddClick = { showAttachmentDialog = !showAttachmentDialog },
                    isUploading = isUploading,
                    uploadProgress = uploadProgress.toFloat()
                )
            }
        },
        containerColor = darkNavy
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(darkNavy)
        ) {
            if (loading && messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cyanAccent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    state = listState
                ) {
                    itemsIndexed(
                        messages,
                        key = { _, message -> message.messageId }
                    ) { index, message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
                        ) {
                            Column {
                                if (shouldShowDateHeader(messages, index)) {
                                    DateHeader(message.timestamp)
                                    Spacer(Modifier.height(12.dp))
                                }
                                when (message.messageType) {
                                    "image" -> ImageMessageBubble(
                                        message = message,
                                        isSentByMe = message.senderId == currentUserId,
                                        onLongPress = { showMessageOptions(context, message, chatViewModel) }
                                    )
                                    "document" -> DocumentMessageBubble(
                                        message = message,
                                        isSentByMe = message.senderId == currentUserId,
                                        onLongPress = { showMessageOptions(context, message, chatViewModel) }
                                    )
                                    else -> MessageBubble(
                                        message = message,
                                        isSentByMe = message.senderId == currentUserId,
                                        onLongPress = { showMessageOptions(context, message, chatViewModel) }
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    receiverName: String,
    receiverImage: String,
    isReceiverTyping: Boolean,
    onBackClick: () -> Unit
) {
    val darkNavy = Color(0xFF000033)
    val cyanAccent = Color(0xFF00FFFF)
    
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = receiverImage,
                    contentDescription = "Profile",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(receiverName, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    AnimatedVisibility(visible = isReceiverTyping) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                            TypingIndicator()
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("typing...", style = TextStyle(fontSize = 13.sp, color = cyanAccent))
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = darkNavy,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAddClick: () -> Unit,
    isUploading: Boolean,
    uploadProgress: Float
) {
    val darkNavy = Color(0xFF000033)
    val cardBg = Color(0xFF1E293B)
    Column(
        modifier = Modifier.background(darkNavy)
    ) {
        if (isUploading) {
            LinearProgressIndicator(
                progress = uploadProgress,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF00FFFF)
            )
        }
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Attachments", tint = Color.White)
            }
            TextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...", color = Color.Gray) },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    disabledContainerColor = cardBg,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSendClick) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun AttachmentPopup(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDocumentClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Attachment") },
        text = { 
            Column {
                TextButton(onClick = onGalleryClick) { Text("Gallery") }
                TextButton(onClick = onCameraClick) { Text("Camera") }
                TextButton(onClick = onDocumentClick) { Text("Document") }
            }
         },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun shouldShowDateHeader(messages: List<ChatMessage>, index: Int): Boolean {
    if (index == 0) return true
    val currentMessage = messages[index]
    val previousMessage = messages[index - 1]
    val cal1 = Calendar.getInstance().apply { timeInMillis = currentMessage.timestamp }
    val cal2 = Calendar.getInstance().apply { timeInMillis = previousMessage.timestamp }
    return cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.YEAR) ||
           cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)
}

@Composable
fun DateHeader(timestamp: Long) {
    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(timestamp))
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = dateString,
            modifier = Modifier.align(Alignment.Center).background(Color(0x80808080), RoundedCornerShape(8.dp)).padding(4.dp),
            color = Color.White
        )
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isSentByMe: Boolean, onLongPress: (ChatMessage) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSentByMe) Color(0xFF005C4B) else Color(0xFF202C33),
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress(message) })
            }
        ) {
            if(message.messageText.isNotEmpty()) {
                Text(
                    text = message.messageText, 
                    modifier = Modifier.padding(10.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ImageMessageBubble(message: ChatMessage, isSentByMe: Boolean, onLongPress: (ChatMessage) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSentByMe) Color(0xFF005C4B) else Color(0xFF202C33),
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress(message) })
            }
        ) {
            AsyncImage(
                model = message.mediaUrl,
                contentDescription = "Image message",
                modifier = Modifier.size(200.dp).padding(4.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun DocumentMessageBubble(message: ChatMessage, isSentByMe: Boolean, onLongPress: (ChatMessage) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSentByMe) Color(0xFF005C4B) else Color(0xFF202C33),
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress(message) }) }
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = "Document", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Document", color = Color.White) // In a real app, you'd parse the filename
            }
        }
    }
}

fun showMessageOptions(context: Context, message: ChatMessage, viewModel: ChatViewModel) {
    val options = arrayOf("Copy", "Delete")
    android.app.AlertDialog.Builder(context)
        .setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("message", message.messageText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    viewModel.deleteMessage(message.chatId, message.messageId)
                }
            }
            dialog.dismiss()
        }
        .show()
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing-indicator")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = index * 100),
                    repeatMode = RepeatMode.Reverse
                ), 
                label = "offsetY-$index"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }
    }
}
