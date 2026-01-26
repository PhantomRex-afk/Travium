package com.example.travium.view

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.MakePostModel
import com.example.travium.model.UserModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel

// Admin-themed dark colors
val AdminDeepNavy = Color(0xFF0F172A)
val AdminCardNavy = Color(0xFF1E293B)
val AdminAccentTeal = Color(0xFF2DD4BF)
val AdminSoftGray = Color(0xFF94A3B8)
val AdminAlertRed = Color(0xFFEF4444)

class AdminDashboardActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        
        setContent {
            val postViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }
            val userViewModel = remember { UserViewModel(UserRepoImpl()) }
            var selectedIndex by remember { mutableIntStateOf(0) }
            
            Scaffold(
                containerColor = AdminDeepNavy,
                topBar = {
                    Column {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Travium", style = TextStyle(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        "Admin Dashboard", style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AdminSoftGray
                                        )
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = AdminCardNavy
                            ),
                            actions = {
                                IconButton(onClick = { /* Handle Notifications */ }) {
                                    BadgedBox(
                                        badge = { }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.notification),
                                            contentDescription = "Notifications",
                                            tint = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                    }
                },
                bottomBar = {
                    Column {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                        NavigationBar(
                            containerColor = AdminCardNavy,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                Triple("Home", R.drawable.outline_home_24, "Home"),
                                Triple("Add Guide", R.drawable.addbox, "Add Guide"),
                                Triple("Users", R.drawable.profile, "Users List")
                            )
                            
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = { Icon(painterResource(item.second), contentDescription = item.third) },
                                    label = { Text(item.first) },
                                    selected = selectedIndex == index,
                                    onClick = { selectedIndex = index },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AdminAccentTeal,
                                        selectedTextColor = AdminAccentTeal,
                                        unselectedIconColor = AdminSoftGray,
                                        unselectedTextColor = AdminSoftGray,
                                        indicatorColor = AdminAccentTeal.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when(selectedIndex) {
                        0 -> AdminHomeFeed(postViewModel, userViewModel)
                        1 -> AddGuideScreen()
                        2 -> AdminUsersList(userViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHomeFeed(postViewModel: MakePostViewModel, userViewModel: UserViewModel) {
    val allPosts by postViewModel.allPosts.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        postViewModel.getAllPosts()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(allPosts) { post ->
            AdminPostCard(post = post, postViewModel = postViewModel, userViewModel = userViewModel)
        }
    }
}

@Composable
fun AddGuideScreen() {
    var placeName by remember { mutableStateOf("") }
    var accommodations by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDeepNavy)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Create New Guide",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        // Place Name Input
        OutlinedTextField(
            value = placeName,
            onValueChange = { placeName = it },
            label = { Text("Place Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = AdminAccentTeal,
                unfocusedLabelColor = AdminSoftGray
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Multiple Images Picker
        Column {
            Text(
                text = "Pictures of the Place",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable { 
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                colors = CardDefaults.cardColors(containerColor = AdminCardNavy),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AdminAccentTeal,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedImageUris.isEmpty()) "Tap to add multiple pictures" else "${selectedImageUris.size} pictures selected",
                            color = AdminSoftGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (selectedImageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Accommodations Input
        OutlinedTextField(
            value = accommodations,
            onValueChange = { accommodations = it },
            label = { Text("Accommodations (Hotels, Resorts, etc.)") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = AdminAccentTeal,
                unfocusedLabelColor = AdminSoftGray
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { /* Future: Save Logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminAccentTeal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Publish Guide",
                color = AdminDeepNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AdminUsersList(userViewModel: UserViewModel) {
    val allUsers by userViewModel.allUsers.observeAsState(initial = emptyList())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Current Users", "Banned Users")

    LaunchedEffect(Unit) {
        userViewModel.getAllUsers()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = AdminDeepNavy,
            contentColor = AdminAccentTeal,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = AdminAccentTeal
                )
            },
            divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    unselectedContentColor = AdminSoftGray
                )
            }
        }

        // Fix: Make banned users list empty as requested
        val filteredUsers = if (selectedTabIndex == 0) allUsers else emptyList<UserModel>()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredUsers) { user ->
                UserCard(user, isBannedView = selectedTabIndex == 1)
            }
        }
    }
}

@Composable
fun UserCard(user: UserModel, isBannedView: Boolean) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardNavy)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = listOf(AdminAccentTeal, Color(0xFF3B82F6)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = user.fullName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = AdminSoftGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.email,
                            color = AdminSoftGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { 
                    val action = if (isBannedView) "Unban" else "Ban"
                    Toast.makeText(context, "$action user logic here", Toast.LENGTH_SHORT).show() 
                }
            ) {
                Icon(
                    imageVector = if (isBannedView) Icons.Default.CheckCircle else Icons.Default.Block,
                    contentDescription = if (isBannedView) "Unban" else "Ban",
                    tint = if (isBannedView) AdminAccentTeal else AdminAlertRed
                )
            }
        }
    }
}

@Composable
fun AdminPostCard(post: MakePostModel, postViewModel: MakePostViewModel, userViewModel: UserViewModel) {
    val context = LocalContext.current
    var author by remember { mutableStateOf<UserModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteReason by remember { mutableStateOf("") }

    LaunchedEffect(post.userId) {
        userViewModel.getUserById(post.userId) { fetchedUser ->
            author = fetchedUser
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AdminCardNavy,
            title = { Text("Delete Post", color = Color.White) },
            text = {
                Column {
                    Text("Are you sure you want to delete this post? This action cannot be undone.", color = AdminSoftGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deleteReason,
                        onValueChange = { deleteReason = it },
                        label = { Text("Reason for deletion") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AdminAccentTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deleteReason.isNotBlank()) {
                            postViewModel.deletePost(post.postId, post.userId, deleteReason) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) showDeleteDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Please provide a reason", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Delete", color = AdminAlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = AdminSoftGray)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardNavy)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = listOf(AdminAccentTeal, Color(0xFF3B82F6)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = (author?.fullName?.take(1) ?: "T").uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = author?.fullName ?: "Explorer", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Post",
                        tint = AdminAlertRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (post.caption.isNotEmpty()) {
                Text(text = post.caption, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
            }
            
            if (post.location.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AdminAccentTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = post.location, color = AdminSoftGray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (post.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun AdminPlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(AdminDeepNavy), contentAlignment = Alignment.Center) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    AdminPlaceholderScreen(title = "Welcome Admin")
}
