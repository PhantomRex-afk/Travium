package com.example.travium.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.travium.model.GroupChat
import com.example.travium.model.GroupMessage
import com.example.travium.repository.GroupChatRepoImpl
import com.example.travium.viewmodel.GroupChatRoomViewModel
import com.example.travium.viewmodel.LeaveGroupResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

val TravelLightBlue = Color(0xFFE3F2FD)

class GroupChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""
        val groupName = intent.getStringExtra("groupName") ?: "Group"
        val currentUserId = intent.getStringExtra("currentUserId") ?: ""
        val currentUserName = intent.getStringExtra("currentUserName") ?: "Me"

        val viewModel = GroupChatRoomViewModel(GroupChatRepoImpl())

        setContent {
            TraviumChatTheme {
                GroupChatScreen(
                    viewModel = viewModel,
                    groupId = groupId,
                    groupName = groupName,
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@Composable
fun TraviumChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = TravelAccentTeal,
            onPrimary = TravelDeepNavy,
            secondary = TravelCardNavy,
            onSecondary = Color.White,
            tertiary = TravelLightBlue,
            onTertiary = TravelDeepNavy,
            background = TravelDeepNavy,
            onBackground = Color.White,
            surface = TravelCardNavy,
            onSurface = Color.White,
            surfaceVariant = TravelCardNavy.copy(alpha = 0.8f),
            onSurfaceVariant = TravelSoftGray,
            error = Color(0xFFF44336),
            onError = Color.White,
            outline = TravelSoftGray.copy(alpha = 0.3f),
            outlineVariant = TravelSoftGray.copy(alpha = 0.1f),
            scrim = Color.Black.copy(alpha = 0.5f)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    viewModel: GroupChatRoomViewModel,
    groupId: String,
    groupName: String,
    currentUserId: String,
    currentUserName: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.observeAsState(emptyList())
    val groupInfo by viewModel.groupInfo.observeAsState()
    val error by viewModel.error.observeAsState()
    val isUploading by viewModel.isUploading.observeAsState(false)
    val uploadProgress by viewModel.uploadProgress.observeAsState(0.0)
    val leaveGroupResult by viewModel.leaveGroupResult.observeAsState()

    var textState by remember { mutableStateOf("") }
    var showGroupInfoDialog by remember { mutableStateOf(false) }
    var showMembersDialog by remember { mutableStateOf(false) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showLeaveGroupDialog by remember { mutableStateOf(false) }
    var showEditMembersDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Show errors
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Load messages and group info on start
    LaunchedEffect(groupId) {
        isLoading = true
        viewModel.loadGroupInfo(groupId)
        viewModel.loadMessages(groupId, limit = 100)
        viewModel.listenForMessages(groupId)
        delay(1000)
        isLoading = false
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle leave group result
    LaunchedEffect(leaveGroupResult) {
        when (leaveGroupResult) {
            is LeaveGroupResult.Success -> {
                Toast.makeText(context, "You have left the group", Toast.LENGTH_SHORT).show()
                onBackPressed()
                viewModel.clearLeaveGroupResult()
            }
            is LeaveGroupResult.Error -> {
                Toast.makeText(context, (leaveGroupResult as LeaveGroupResult.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.clearLeaveGroupResult()
            }
            null -> {}
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.sendGroupImage(context, groupId, currentUserId, currentUserName, it)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            viewModel.sendGroupImage(context, groupId, currentUserId, currentUserName, selectedImageUri!!)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createImageUri(context)
            selectedImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TraviumGroupChatTopBar(
                groupName = groupName,
                groupImage = groupInfo?.groupImage ?: "",
                memberCount = groupInfo?.members?.size ?: 0,
                onBackPressed = onBackPressed,
                onGroupInfoClick = { showGroupInfoDialog = true },
                onMembersClick = { showMembersDialog = true },
                onSettingsClick = { showSettingsMenu = true },
                isCurrentUserCreator = groupInfo?.createdBy == currentUserId
            )
        },
        bottomBar = {
            TraviumChatInputBar(
                text = textState,
                onTextChange = { textState = it },
                onSend = {
                    if (textState.isNotBlank()) {
                        viewModel.sendGroupMessage(groupId, currentUserId, currentUserName, textState)
                        textState = ""
                        coroutineScope.launch {
                            delay(100)
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                },
                onAttachmentClick = { showAttachmentOptions = true },
                isUploading = isUploading,
                uploadProgress = uploadProgress
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TravelDeepNavy)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = TravelAccentTeal,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading messages...",
                            color = TravelAccentTeal,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TravelSoftGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No messages yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TravelAccentTeal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start the conversation!",
                            fontSize = 14.sp,
                            color = TravelSoftGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(
                        items = messages,
                        key = { it.messageId }
                    ) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 30 }),
                            modifier = Modifier.animateItem(
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        ) {
                            Column {
                                TraviumGroupMessageItem(
                                    message = message,
                                    isMe = message.senderId == currentUserId,
                                    groupInfo = groupInfo,
                                    onMessageLongPress = {
                                        viewModel.deleteMessage(groupId, message.messageId)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Settings menu
    if (showSettingsMenu) {
        TraviumGroupSettingsMenu(
            onDismiss = { showSettingsMenu = false },
            onLeaveGroup = {
                showSettingsMenu = false
                showLeaveGroupDialog = true
            },
            onEditMembers = {
                showSettingsMenu = false
                showEditMembersDialog = true
            },
            isCreator = groupInfo?.createdBy == currentUserId
        )
    }

    // Leave group confirmation dialog
    if (showLeaveGroupDialog) {
        TraviumLeaveGroupDialog(
            onDismiss = { showLeaveGroupDialog = false },
            onConfirmLeave = {
                showLeaveGroupDialog = false
                viewModel.leaveGroup(groupId, currentUserId)
            },
            isCreator = groupInfo?.createdBy == currentUserId
        )
    }

    // Edit members dialog
    if (showEditMembersDialog && groupInfo != null) {
        TraviumEditMembersDialog(
            groupInfo = groupInfo!!,
            currentUserId = currentUserId,
            onDismiss = { showEditMembersDialog = false },
            onRemoveMember = { memberId ->
                // Implement remove member logic
                Toast.makeText(context, "Remove member: $memberId", Toast.LENGTH_SHORT).show()
            },
            onAddMember = {
                // Implement add member logic
                Toast.makeText(context, "Add member clicked", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Other dialogs
    if (showGroupInfoDialog && groupInfo != null) {
        TraviumGroupInfoDialog(
            groupInfo = groupInfo!!,
            onDismiss = { showGroupInfoDialog = false }
        )
    }

    if (showMembersDialog && groupInfo != null) {
        TraviumGroupMembersDialog(
            groupInfo = groupInfo!!,
            currentUserId = currentUserId,
            onDismiss = { showMembersDialog = false }
        )
    }

    if (showAttachmentOptions) {
        TraviumAttachmentPopup(
            onDismiss = { showAttachmentOptions = false },
            onImageClick = {
                showAttachmentOptions = false
                imagePickerLauncher.launch("image/*")
            },
            onCameraClick = {
                showAttachmentOptions = false
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                        val uri = viewModel.createImageUri(context)
                        selectedImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraviumGroupChatTopBar(
    groupName: String,
    groupImage: String,
    memberCount: Int,
    onBackPressed: () -> Unit,
    onGroupInfoClick: () -> Unit,
    onMembersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isCurrentUserCreator: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(6.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = TravelCardNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(TravelAccentTeal.copy(alpha = 0.2f), CircleShape)
                    .clickable { onBackPressed() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = TravelAccentTeal,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Group Image
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(TravelAccentTeal)
                    .clickable { onGroupInfoClick() }
            ) {
                if (groupImage.isNotEmpty()) {
                    AsyncImage(
                        model = groupImage,
                        contentDescription = "Group Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = groupName.firstOrNull()?.uppercase() ?: "G",
                            color = TravelDeepNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Group Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGroupInfoClick() }
            ) {
                Text(
                    text = groupName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TravelAccentTeal
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$memberCount members",
                        fontSize = 13.sp,
                        color = TravelSoftGray
                    )
                    if (isCurrentUserCreator) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(TravelAccentTeal, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Admin",
                                fontSize = 10.sp,
                                color = TravelDeepNavy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onMembersClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Members",
                        tint = TravelAccentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = TravelAccentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TraviumGroupSettingsMenu(
    onDismiss: () -> Unit,
    onLeaveGroup: () -> Unit,
    onEditMembers: () -> Unit,
    isCreator: Boolean
) {
    val density = LocalDensity.current

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(
            x = with(density) { (-16).dp.roundToPx() },
            y = with(density) { 80.dp.roundToPx() }
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically { -20 },
            exit = fadeOut() + scaleOut() + slideOutVertically { -20 }
        ) {
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TravelCardNavy)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Edit Members (only for creator)
                    if (isCreator) {
                        TraviumSettingsMenuItem(
                            icon = Icons.Default.Edit,
                            text = "Edit Members",
                            iconColor = TravelAccentTeal,
                            onClick = onEditMembers
                        )
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = TravelSoftGray.copy(alpha = 0.3f)
                        )
                    }

                    // Leave Group
                    TraviumSettingsMenuItem(
                        icon = Icons.Default.ExitToApp,
                        text = "Leave Group",
                        iconColor = Color(0xFFF44336),
                        textColor = Color(0xFFF44336),
                        onClick = onLeaveGroup
                    )
                }
            }
        }
    }
}

@Composable
fun TraviumSettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconColor: Color,
    textColor: Color = Color.White,
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
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
}

@Composable
fun TraviumLeaveGroupDialog(
    onDismiss: () -> Unit,
    onConfirmLeave: () -> Unit,
    isCreator: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isCreator) "Cannot Leave Group" else "Leave Group?",
                color = if (isCreator) Color(0xFFF44336) else TravelAccentTeal
            )
        },
        text = {
            Text(
                text = if (isCreator)
                    "You are the group creator. You cannot leave the group. If you want to leave, you must delete the group or transfer ownership first."
                else "Are you sure you want to leave this group? You won't be able to see messages anymore.",
                fontSize = 14.sp,
                color = Color.White
            )
        },
        confirmButton = {
            if (!isCreator) {
                TextButton(
                    onClick = onConfirmLeave,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336))
                ) {
                    Text("Leave")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isCreator) "OK" else "Cancel", color = TravelAccentTeal)
            }
        },
        containerColor = TravelCardNavy,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun TraviumEditMembersDialog(
    groupInfo: GroupChat,
    currentUserId: String,
    onDismiss: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onAddMember: () -> Unit
) {
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var selectedMemberName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TravelCardNavy),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Members",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onAddMember,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Member",
                            tint = TravelAccentTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = "${groupInfo.members.size} members",
                    fontSize = 14.sp,
                    color = TravelSoftGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(groupInfo.members.indices.toList()) { index ->
                        val memberId = groupInfo.members[index]
                        val memberName = groupInfo.memberNames.getOrNull(index) ?: "Unknown"
                        val memberPhoto = groupInfo.memberPhotos.getOrNull(index) ?: ""
                        val isCreator = memberId == groupInfo.createdBy
                        val isCurrentUser = memberId == currentUserId

                        TraviumEditableMemberItem(
                            memberId = memberId,
                            memberName = memberName,
                            memberPhoto = memberPhoto,
                            isCreator = isCreator,
                            isCurrentUser = isCurrentUser,
                            onRemoveClick = {
                                if (!isCreator && !isCurrentUser) {
                                    selectedMemberId = memberId
                                    selectedMemberName = memberName
                                    showRemoveConfirmation = true
                                }
                            }
                        )
                        if (index < groupInfo.members.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = TravelSoftGray.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TravelAccentTeal,
                        contentColor = TravelDeepNavy
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Remove confirmation dialog
    if (showRemoveConfirmation && selectedMemberId != null) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = { Text("Remove Member", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to remove $selectedMemberName from the group?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveMember(selectedMemberId!!)
                        showRemoveConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336))
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TravelAccentTeal)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = TravelCardNavy
        )
    }
}

@Composable
fun TraviumEditableMemberItem(
    memberId: String,
    memberName: String,
    memberPhoto: String,
    isCreator: Boolean,
    isCurrentUser: Boolean,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Member photo
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(TravelAccentTeal)
        ) {
            if (memberPhoto.isNotEmpty()) {
                AsyncImage(
                    model = memberPhoto,
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
                        text = memberName.firstOrNull()?.uppercase() ?: "?",
                        color = TravelDeepNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCurrentUser) "$memberName (You)" else memberName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White
                )
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(TravelAccentTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "You",
                            fontSize = 10.sp,
                            color = TravelAccentTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (isCreator) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Group Admin",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA726),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Remove button (not shown for creator or current user)
        if (!isCreator && !isCurrentUser) {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = TravelSoftGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TraviumGroupMessageItem(
    message: GroupMessage,
    isMe: Boolean,
    groupInfo: GroupChat?,
    onMessageLongPress: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start

    val senderIndex = groupInfo?.members?.indexOf(message.senderId) ?: -1
    val senderName = if (senderIndex >= 0) {
        groupInfo?.memberNames?.getOrNull(senderIndex) ?: message.senderName
    } else message.senderName

    val senderPhoto = if (senderIndex >= 0) {
        groupInfo?.memberPhotos?.getOrNull(senderIndex) ?: ""
    } else ""

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isMe) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(TravelAccentTeal)
                ) {
                    if (senderPhoto.isNotEmpty()) {
                        AsyncImage(
                            model = senderPhoto,
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
                                text = senderName.firstOrNull()?.uppercase() ?: "?",
                                color = TravelDeepNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
            ) {
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TravelAccentTeal,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = { onMessageLongPress() })
                        },
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) TravelAccentTeal else TravelCardNavy
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (message.messageType) {
                            "text" -> {
                                Text(
                                    text = message.messageText,
                                    color = if (isMe) TravelDeepNavy else Color.White,
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp
                                )
                            }
                            "image" -> {
                                AsyncImage(
                                    model = message.messageText,
                                    contentDescription = "Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            "voice" -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Voice",
                                        tint = if (isMe) TravelDeepNavy else TravelAccentTeal,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Voice message",
                                        fontSize = 14.sp,
                                        color = if (isMe) TravelDeepNavy else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatMessageTimes(message.timestamp),
                                fontSize = 10.sp,
                                color = if (isMe) TravelDeepNavy.copy(0.8f) else TravelSoftGray
                            )

                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = if (message.isRead) "Read" else "Sent",
                                    tint = if (message.isRead) Color(0xFF00E676) else TravelDeepNavy.copy(0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isMe) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun TraviumChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Double = 0.0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            if (isUploading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progressValue = (uploadProgress.toFloat() / 100f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier.size(24.dp),
                        color = TravelAccentTeal,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        trackColor = TravelSoftGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Uploading ${uploadProgress.toInt()}%",
                        fontSize = 14.sp,
                        color = TravelAccentTeal
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            TravelAccentTeal,
                            CircleShape
                        )
                        .clickable(enabled = !isUploading) { onAttachmentClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach",
                        tint = TravelDeepNavy,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TravelDeepNavy)
                ) {
                    TextField(
                        value = text,
                        onValueChange = onTextChange,
                        placeholder = {
                            Text(
                                text = "Type a message...",
                                fontSize = 15.sp,
                                color = TravelSoftGray
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = TravelAccentTeal
                        ),
                        textStyle = TextStyle(fontSize = 15.sp),
                        maxLines = 4,
                        enabled = !isUploading
                    )
                }

                AnimatedContent(
                    targetState = text.isNotBlank(),
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    },
                    label = "sendButton"
                ) { hasText ->
                    if (hasText) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    TravelAccentTeal,
                                    CircleShape
                                )
                                .clickable { onSend() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = TravelDeepNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(TravelSoftGray.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = TravelSoftGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TraviumGroupInfoDialog(groupInfo: GroupChat, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TravelCardNavy),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(TravelAccentTeal)
                ) {
                    if (groupInfo.groupImage.isNotEmpty()) {
                        AsyncImage(
                            model = groupInfo.groupImage,
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
                                text = groupInfo.groupName.firstOrNull()?.uppercase() ?: "G",
                                color = TravelDeepNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = groupInfo.groupName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TravelAccentTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${groupInfo.members.size} members",
                        fontSize = 15.sp,
                        color = TravelSoftGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Created by ${groupInfo.createdByName}",
                    fontSize = 13.sp,
                    color = TravelSoftGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TravelAccentTeal,
                        contentColor = TravelDeepNavy
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TraviumGroupMembersDialog(
    groupInfo: GroupChat,
    currentUserId: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TravelCardNavy),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Group Members",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "${groupInfo.members.size} members",
                    fontSize = 14.sp,
                    color = TravelSoftGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(groupInfo.members.indices.toList()) { index ->
                        TraviumMemberItem(
                            memberName = groupInfo.memberNames.getOrNull(index) ?: "Unknown",
                            memberPhoto = groupInfo.memberPhotos.getOrNull(index) ?: "",
                            isCreator = groupInfo.members[index] == groupInfo.createdBy,
                            isCurrentUser = groupInfo.members[index] == currentUserId
                        )
                        if (index < groupInfo.members.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = TravelSoftGray.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TravelAccentTeal,
                        contentColor = TravelDeepNavy
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TraviumMemberItem(
    memberName: String,
    memberPhoto: String,
    isCreator: Boolean,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(TravelAccentTeal)
        ) {
            if (memberPhoto.isNotEmpty()) {
                AsyncImage(
                    model = memberPhoto,
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
                        text = memberName.firstOrNull()?.uppercase() ?: "?",
                        color = TravelDeepNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCurrentUser) "$memberName (You)" else memberName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White
                )
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(TravelAccentTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "You",
                            fontSize = 10.sp,
                            color = TravelAccentTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (isCreator) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Group Admin",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA726),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TraviumAttachmentPopup(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit
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
            enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically { 50 },
            exit = fadeOut() + scaleOut() + slideOutVertically { 50 }
        ) {
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TravelCardNavy)
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
                            color = Color.White
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    TraviumAttachmentOption(
                        icon = Icons.Default.AccountCircle,
                        text = "Gallery",
                        gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                        onClick = onImageClick
                    )

                    TraviumAttachmentOption(
                        icon = Icons.Default.Face,
                        text = "Camera",
                        gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
                        onClick = onCameraClick
                    )
                }
            }
        }
    }
}

@Composable
fun TraviumAttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TravelDeepNavy)
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
                    imageVector = icon,
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
                    color = Color.White
                )
            )
        }
    }
}

fun formatMessageTimes(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}