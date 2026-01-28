package com.example.travium.view

import android.Manifest
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.travium.R
import com.example.travium.model.ChatMessage
import com.example.travium.repository.GroupChatRepository
import com.example.travium.Repository.GroupChatRepositoryImpl
import com.example.travium.repository.ChatRepositoryImpl
import com.example.travium.viewmodel.ChatViewModel
import com.example.travium.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.travium.view.ui.theme.Purple40


class ChatActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            ChatRepositoryImpl(),
            groupChatRepository = GroupChatRepositoryImpl())
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Microphone permission is required for voice messages",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val receiverId = intent.getStringExtra("receiverId") ?: ""
        val receiverName = intent.getStringExtra("receiverName") ?: "User"
        val receiverImage = intent.getStringExtra("receiverImage") ?:""
        val currentUserId = intent.getStringExtra("currentUserId") ?: ""
        val currentUserName = intent.getStringExtra("currentUserName") ?: ""

        chatViewModel.getOrCreateChatRoom(
            participant1Id = currentUserId,
            participant2Id = receiverId,
            participant1Name = currentUserName,
            participant2Name = receiverName,
            participant1Photo = "",
            participant2Photo = receiverImage
        )

        setContent {
            ChatBody(
                receiverName = receiverName,
                receiverImage = receiverImage,
                receiverId = receiverId,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                chatViewModel = chatViewModel,
                onRequestPermission = { checkAndRequestPermission() }
            )
        }
    }

    private fun checkAndRequestPermission(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> true
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                false
            }
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
    chatViewModel: ChatViewModel,
    onRequestPermission: () -> Boolean
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val keyboardController = LocalSoftwareKeyboardController.current

    var messageText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
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
    var recordingJob by remember { mutableStateOf<Job?>(null) }


    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            Log.e("ChatActivity", "Error: $it")
            chatViewModel.clearError()
        }
    }

    LaunchedEffect(chatRoom) {
        chatRoom?.chatId?.let { chatId ->
            chatViewModel.loadMessages(chatId)
            chatViewModel.listenForNewMessages(chatId)
            chatViewModel.listenForTypingStatus(chatId)
            chatViewModel.markMessagesAsRead(chatId, currentUserId)
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isReceiverTyping = remember(typingStatus, receiverId) {
        typingStatus?.let { (userId, typing) ->
            userId == receiverId && typing
        } ?: false
    }

    LaunchedEffect(messageText) {
        typingJob?.cancel()
        if (messageText.isNotEmpty()) {
            typingJob = coroutineScope.launch {
                chatRoom?.chatId?.let {
                    chatViewModel.setTypingStatus(it, currentUserId, true)
                    delay(2000)
                    chatViewModel.setTypingStatus(it, currentUserId, false)
                }
            }
        } else {
            chatRoom?.chatId?.let { chatViewModel.setTypingStatus(it, currentUserId, false) }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatRoom?.let { cr ->
                chatViewModel.uploadAndSendMediaMessage(
                    context = context,
                    mediaUri = uri,
                    mediaType = "image",
                    chatId = cr.chatId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    senderName = currentUserName,
                    receiverName = receiverName,
                )
            }
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatRoom?.let { cr ->
                chatViewModel.uploadAndSendMediaMessage(
                    context = context,
                    mediaUri = uri,
                    mediaType = "document",
                    chatId = cr.chatId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    senderName = currentUserName,
                    receiverName = receiverName
                )
            }
        }
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            chatRoom?.let { cr ->
                chatViewModel.uploadAndSendMediaMessage(
                    context = context,
                    mediaUri = tempPhotoUri!!,
                    mediaType = "image",
                    chatId = cr.chatId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    senderName = currentUserName,
                    receiverName = receiverName
                )
            }
        }
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                receiverName = receiverName,
                receiverImage = receiverImage,
                isReceiverTyping = isReceiverTyping,
                onBackClick = { activity?.finish() },
                onInfoClick = { /* TODO */ }
            )
        },
        bottomBar = {
            Box {
                if (showAttachmentDialog) {
                    AttachmentPopup(
                        onDismiss = { showAttachmentDialog = false },
                        onGalleryClick = {
                            showAttachmentDialog = false
                            imagePickerLauncher.launch("image/*")
                        },
                        onCameraClick = {
                            showAttachmentDialog = false
                            val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
                            tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
                            cameraLauncher.launch(tempPhotoUri!!)
                        },
                        onDocumentClick = {
                            showAttachmentDialog = false
                            documentPickerLauncher.launch("*/*")
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F4F8),
                            Color(0xFFF0F8FF)
                        )
                    )
                )
        ) {
            if (loading && messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple40)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    state = listState,
                    reverseLayout = false,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    itemsIndexed(
                        messages,
                        key = { _, message -> message.messageId }
                    ) { index, message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
//                            modifier = Modifier.animateItemPlacement()
                        ) {
                            Column {
                                if (shouldShowDateHeader(messages, index)) {
                                    DateHeader(message.timestamp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                when (message.messageType) {
                                    "image" -> ImageMessageBubble(
                                        message = message,
                                        isSentByMe = message.senderId == currentUserId,
                                        onLongPress = { showMessageOptions(context, message, chatViewModel) }
                                    )
                                    "video" -> VideoMessageBubble(
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
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ChatTopAppBar(
    receiverName: String,
    receiverImage: String,
    isReceiverTyping: Boolean,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(4.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF1976D2)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    AsyncImage(
                        model = receiverImage,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = receiverName,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    )
                    AnimatedVisibility(visible = isReceiverTyping) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TypingIndicator()
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "typing",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color(0xFF1976D2)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "typing$index")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = index * 100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offsetY$index"
            )

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = offsetY.dp)
                    .background(Color(0xFF1976D2), CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAddClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Double = 0.0,
    showAttachmentDialog: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .shadow(8.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Purple40, Color(0xFF6650a4))
                        ),
                        shape = CircleShape
                    )
                    .clickable(enabled = !isUploading) { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_add_24),
                    contentDescription = "Attachment",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isUploading) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = uploadProgress.toFloat() / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Purple40,
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uploading ${uploadProgress.toInt()}%",
                                style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                            )
                        }
                    } else {
                        TextField(
                            value = messageText,
                            onValueChange = onMessageChange,
                            placeholder = {
                                Text(
                                    text = "Type a message...",
                                    style = TextStyle(fontSize = 15.sp, color = Color.Gray)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                            singleLine = false,
                            maxLines = 3
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = messageText.isNotBlank(),
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                },
                label = "iconTransition"
            ) { hasText ->
                if (hasText) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Purple40, Color(0xFF42A5F5))
                                ),
                                shape = CircleShape
                            )
                            .clickable { onSendClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
    val density = LocalDensity.current

    Popup(
        alignment = Alignment.BottomStart,
        offset = IntOffset(
            x = with(density) { 16.dp.roundToPx() },
            y = with(density) { (-100).dp.roundToPx() }
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically(initialOffsetY = { 50 }),
            exit = fadeOut() + scaleOut() + slideOutVertically(targetOffsetY = { 50 })
        ) {
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Send Attachment",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    AttachmentOptionItem(
                        icon = R.drawable.image,
                        text = "Gallery",
                        gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                        onClick = onGalleryClick
                    )

                    AttachmentOptionItem(
                        icon = R.drawable.outline_add_a_photo_24,
                        text = "Camera",
                        gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
                        onClick = onCameraClick
                    )

                    AttachmentOptionItem(
                        icon = R.drawable.outline_add_24,
                        text = "Document",
                        gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
                        onClick = onDocumentClick
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentOptionItem(
    icon: Int,
    text: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = text,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean,
    onLongPress: () -> Unit
) {
    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByMe) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF42A5F5), Purple40)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.White, Color.White)
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isSentByMe) 18.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.background(bubbleColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.messageText,
                        color = if (isSentByMe) Color.White else Color(0xFF212121),
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formatMessageTime(message.timestamp),
                            color = if (isSentByMe) Color.White.copy(alpha = 0.8f) else Color.Gray,
                            fontSize = 11.sp
                        )
                        if (isSentByMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (message.isRead) Icons.Default.Done else Icons.Default.Done,
                                contentDescription = "Status",
                                modifier = Modifier.size(14.dp),
                                tint = if (message.isRead) Color(0xFF00C853) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(timestamp: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = formatDates(timestamp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )
        }
    }
}


@Composable
fun ImageMessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean,
    onLongPress: () -> Unit
) {
    val context = LocalContext.current
    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(message.mediaUrl), "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    )
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    if (isSentByMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Status",
                            modifier = Modifier.size(14.dp),
                            tint = if (message.isRead) Color(0xFF00C853) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoMessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean,
    onLongPress: () -> Unit
) {
    val context = LocalContext.current
    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(message.mediaUrl), "video/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    )
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A237E), Color(0xFF283593))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF1976D2)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎥 Video",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatMessageTime(message.timestamp),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        if (isSentByMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Status",
                                modifier = Modifier.size(14.dp),
                                tint = if (message.isRead) Color(0xFF00C853) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentMessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean,
    onLongPress: () -> Unit
) {
    val context = LocalContext.current
    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(2.dp, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(message.mediaUrl), "*/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No app found to open this document", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = "Document",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📄 Document",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "Tap to open",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    if (isSentByMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Status",
                            modifier = Modifier.size(14.dp),
                            tint = if (message.isRead) Color(0xFF00C853) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

fun formatMessageTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}

fun formatDates(timestamp: Long): String {
    val date = Date(timestamp)
    val today = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { time = date }

    return when {
        today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) -> "Today"

        today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) - 1 == messageDate.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun shouldShowDateHeader(messages: List<ChatMessage>, index: Int): Boolean {
    if (index == 0) return true

    val currentMessage = messages[index]
    val previousMessage = messages[index - 1]

    val currentDate = Calendar.getInstance().apply {
        timeInMillis = currentMessage.timestamp
    }
    val previousDate = Calendar.getInstance().apply {
        timeInMillis = previousMessage.timestamp
    }

    return currentDate.get(Calendar.DAY_OF_YEAR) != previousDate.get(Calendar.DAY_OF_YEAR) ||
            currentDate.get(Calendar.YEAR) != previousDate.get(Calendar.YEAR)
}

fun showMessageOptions(context: Context, message: ChatMessage, chatViewModel: ChatViewModel) {
    val options = if (message.messageType == "voice") {
        arrayOf("Delete Message")
    } else {
        arrayOf("Copy Text", "Delete Message")
    }

    AlertDialog.Builder(context)
        .setTitle("Message Options")
        .setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if (message.messageType == "voice") {
                        chatViewModel.deleteMessage(message.messageId, message.chatId)
                    } else {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("message", message.messageText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                    }
                }
                1 -> {
                    chatViewModel.deleteMessage(message.messageId, message.chatId)
                }
            }
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        .show()
}