package com.example.travium.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.travium.repository.GroupChatRepoImpl
import com.example.travium.model.UserModel
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.CreateGroupChatViewModel
import com.example.travium.viewmodel.CreateGroupUiState
import com.example.travium.viewmodel.ValidationResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class CreateGroupChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

        setContent {
            CreateGroupChatScreen(
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                onBack = { finish() },
                onGroupCreated = { groupId ->
                    Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupChatScreen(
    currentUserId: String,
    currentUserName: String,
    onBack: () -> Unit,
    onGroupCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize ViewModel with repositories
    val viewModel = remember {
        CreateGroupChatViewModel().apply {
            initRepositories(
                groupChatRepo = GroupChatRepoImpl(),
                userRepo = UserRepoImpl()
            )
        }
    }

    // Load current user and contacts
    LaunchedEffect(currentUserId) {
        viewModel.loadCurrentUser(currentUserId)
        viewModel.loadFollowContacts() // Load only followers/following
    }

    // Observe ViewModel state
    val uiState by viewModel.uiState.collectAsState()
    val availableContacts by viewModel.availableContacts.collectAsState()
    val selectedContacts by viewModel.selectedContacts.collectAsState()
    val groupName by viewModel.groupName.collectAsState()
    val groupImage by viewModel.groupImage.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Filter contacts based on search
    val filteredContacts = if (searchQuery.isBlank()) {
        availableContacts
    } else {
        availableContacts.filter { user ->
            user.fullName.contains(searchQuery, ignoreCase = true) ||
                    user.username.contains(searchQuery, ignoreCase = true)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle the captured image
            Toast.makeText(context, "Photo taken successfully", Toast.LENGTH_SHORT).show()
            // Set the image from camera
            cameraImageUri?.let { uri ->
                viewModel.setGroupImage(uri.toString())
            }
        } else {
            Toast.makeText(context, "Failed to take photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, open camera
            val timeStamp = System.currentTimeMillis()
            val storageDir = context.cacheDir
            val imageFile = File.createTempFile(
                "group_photo_${timeStamp}",
                ".jpg",
                storageDir
            )

            // Update the cameraImageUri state
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            } else {
                Uri.fromFile(imageFile)
            }

            cameraImageUri = uri

            // Launch camera with the URI
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Image picker launcher for gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Handle image upload - you can implement Cloudinary upload here
            Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
            // Set the image URI
            viewModel.setGroupImage(it.toString())
        }
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateGroupUiState.Error -> {
                val errorMessage = (uiState as CreateGroupUiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            is CreateGroupUiState.GroupCreated -> {
                val groupId = (uiState as CreateGroupUiState.GroupCreated).groupId
                onGroupCreated(groupId)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create New Group",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelCardNavy
                ),
                actions = {
                    // Create button in top bar
                    IconButton(
                        onClick = {
                            if (viewModel.validateGroupCreation() is ValidationResult.Valid) {
                                viewModel.createGroup()
                            } else {
                                val validation = viewModel.validateGroupCreation()
                                if (validation is ValidationResult.Error) {
                                    Toast.makeText(context, validation.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = groupName.isNotEmpty() && selectedContacts.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Create Group",
                            tint = if (groupName.isNotEmpty() && selectedContacts.isNotEmpty())
                                TravelAccentTeal else TravelSoftGray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(TravelDeepNavy)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Group Profile Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    // Group Image
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(TravelCardNavy)
                            .clickable { showImagePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (groupImage != null) {
                            Image(
                                painter = rememberAsyncImagePainter(groupImage),
                                contentDescription = "Group Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Add Photo",
                                    tint = TravelAccentTeal,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Add Photo",
                                    color = TravelAccentTeal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Group Name Input
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { viewModel.updateGroupName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Enter group name",
                                color = TravelSoftGray
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TravelAccentTeal,
                            unfocusedBorderColor = TravelSoftGray,
                            focusedContainerColor = TravelCardNavy,
                            unfocusedContainerColor = TravelCardNavy
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Selected Users Summary
                if (selectedContacts.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TravelCardNavy
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Selected (${selectedContacts.size})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                TextButton(
                                    onClick = { viewModel.clearSelection() }
                                ) {
                                    Text(
                                        "Clear All",
                                        color = TravelAccentTeal
                                    )
                                }
                            }

                            // Show selected user avatars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val selectedUsers = viewModel.getSelectedUsers()
                                selectedUsers.take(5).forEach { user ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TravelDeepNavy),
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
                                                text = user.fullName.take(1).uppercase(),
                                                color = TravelAccentTeal,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                if (selectedUsers.size > 5) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TravelAccentTeal.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${selectedUsers.size - 5}",
                                            color = TravelAccentTeal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Search followers...",
                            color = TravelSoftGray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TravelAccentTeal
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TravelAccentTeal,
                        unfocusedBorderColor = TravelSoftGray,
                        focusedContainerColor = TravelCardNavy,
                        unfocusedContainerColor = TravelCardNavy
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contacts List
                if (filteredContacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "No Contacts",
                                modifier = Modifier.size(64.dp),
                                tint = TravelSoftGray
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "No contacts found for '$searchQuery'"
                                } else {
                                    "No followers/following to add"
                                },
                                color = TravelSoftGray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredContacts,
                            key = { user -> user.userId }
                        ) { user ->
                            UserSelectionItem(
                                user = user,
                                isSelected = selectedContacts.contains(user.userId),
                                onSelect = { userId ->
                                    viewModel.toggleContactSelection(userId)
                                }
                            )
                        }
                    }
                }
            }

            // Loading overlay
            if (uiState is CreateGroupUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TravelCardNavy
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = TravelAccentTeal)
                            Text(
                                "Creating group...",
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Upload progress overlay
            if (uiState is CreateGroupUiState.Uploading && uploadProgress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TravelCardNavy
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { uploadProgress.toFloat() / 100f },
                                color = TravelAccentTeal
                            )
                            Text(
                                "Uploading image... ${uploadProgress.toInt()}%",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Image picker dialog
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Choose Group Photo", color = Color.White) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showImagePicker = false
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = TravelCardNavy,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null)
                            Text("Choose from Gallery")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showImagePicker = false
                            // Check camera permission first
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = TravelCardNavy,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Text("Take Photo")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showImagePicker = false
                            viewModel.setGroupImage(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = TravelCardNavy,
                            contentColor = TravelAccentTeal
                        )
                    ) {
                        Text("Remove Photo")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Cancel", color = TravelAccentTeal)
                }
            },
            containerColor = TravelCardNavy,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

@Composable
fun UserSelectionItem(
    user: UserModel,
    isSelected: Boolean,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(user.userId) },
        colors = CardDefaults.cardColors(
            containerColor = TravelCardNavy
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selection checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) TravelAccentTeal else TravelDeepNavy)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) TravelAccentTeal else TravelSoftGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // User avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TravelDeepNavy),
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
                        text = user.fullName.take(1).uppercase(),
                        color = TravelAccentTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.fullName,
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

            // Status indicator
            // You could add online status or follow status here
        }
    }
}