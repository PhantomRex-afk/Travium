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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.shadow
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
import com.example.travium.model.GuideModel
import com.example.travium.model.MakePostModel
import com.example.travium.model.UserModel
import com.example.travium.repository.GuideRepoImpl
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.GuideViewModel
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel

// Enhanced Admin-themed colors
val AdminDeepNavy = Color(0xFF020617) // Even deeper navy
val AdminCardNavy = Color(0xFF1E293B)
val AdminAccentTeal = Color(0xFF2DD4BF)
val AdminSoftGray = Color(0xFF94A3B8)
val AdminAlertRed = Color(0xFFF43F5E) // Vibrant rose red
val AdminGlowTeal = AdminAccentTeal.copy(alpha = 0.15f)

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
            val guideViewModel = remember { GuideViewModel(GuideRepoImpl()) }
            var selectedIndex by remember { mutableIntStateOf(0) }
            
            Scaffold(
                containerColor = AdminDeepNavy,
                topBar = {
                    Surface(
                        color = AdminCardNavy,
                        shadowElevation = 8.dp
                    ) {
                        Column {
                            CenterAlignedTopAppBar(
                                title = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Travium", style = TextStyle(
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        Text(
                                            "Admin Control Center", style = TextStyle(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AdminAccentTeal,
                                                letterSpacing = 2.sp
                                            )
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color.Transparent
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
                            HorizontalDivider(color = AdminAccentTeal.copy(alpha = 0.2f), thickness = 1.dp)
                        }
                    }
                },
                bottomBar = {
                    Surface(
                        color = AdminCardNavy,
                        shadowElevation = 16.dp
                    ) {
                        Column {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp
                            ) {
                                val items = listOf(
                                    Triple("Home", R.drawable.outline_home_24, "Home"),
                                    Triple("Add", R.drawable.addbox, "Add Guide"),
                                    Triple("Manage", R.drawable.outline_map_pin_review_24, "Manage Guides"),
                                    Triple("Users", R.drawable.profile, "Users List")
                                )
                                
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        icon = { Icon(painterResource(item.second), contentDescription = item.third) },
                                        label = { Text(item.first, fontWeight = FontWeight.Bold) },
                                        selected = selectedIndex == index,
                                        onClick = { selectedIndex = index },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = AdminAccentTeal,
                                            selectedTextColor = AdminAccentTeal,
                                            unselectedIconColor = AdminSoftGray,
                                            unselectedTextColor = AdminSoftGray,
                                            indicatorColor = AdminAccentTeal.copy(alpha = 0.12f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AdminDeepNavy, Color(0xFF0F172A))
                        )
                    )
                ) {
                    when(selectedIndex) {
                        0 -> AdminHomeFeed(postViewModel, userViewModel)
                        1 -> AddGuideScreen(guideViewModel)
                        2 -> AdminGuideList(guideViewModel)
                        3 -> AdminUsersList(userViewModel)
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(allPosts) { post ->
            AdminPostCard(post = post, postViewModel = postViewModel, userViewModel = userViewModel)
        }
    }
}

@Composable
fun AddGuideScreen(guideViewModel: GuideViewModel) {
    val context = LocalContext.current
    
    var placeName by remember { mutableStateOf("") }
    var accommodations by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPublishing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Create New Journey Guide",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            letterSpacing = (-0.5).sp
        )

        // Place Name Input
        OutlinedTextField(
            value = placeName,
            onValueChange = { placeName = it },
            label = { Text("Destination Name") },
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedLabelColor = AdminAccentTeal,
                unfocusedLabelColor = AdminSoftGray,
                focusedContainerColor = AdminCardNavy,
                unfocusedContainerColor = AdminCardNavy
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Multiple Images Picker - Premium UI
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Gallery Images",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (selectedImageUris.isNotEmpty()) {
                    Text(
                        text = "${selectedImageUris.size} selected",
                        color = AdminAccentTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (selectedImageUris.isEmpty()) 180.dp else 150.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardNavy),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (selectedImageUris.isEmpty()) Color.White.copy(alpha = 0.1f) else AdminAccentTeal.copy(alpha = 0.5f))
            ) {
                if (selectedImageUris.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = AdminAccentTeal,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Select Destination Photos",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(selectedImageUris) { uri ->
                            Box(modifier = Modifier.size(120.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AdminAlertRed)
                                        .clickable { 
                                            selectedImageUris = selectedImageUris.filter { it != uri }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        item {
                            Surface(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                color = AdminAccentTeal.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, AdminAccentTeal.copy(alpha = 0.3f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add more",
                                        tint = AdminAccentTeal,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Accommodations Input
        OutlinedTextField(
            value = accommodations,
            onValueChange = { accommodations = it },
            label = { Text("Accommodation Details") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedLabelColor = AdminAccentTeal,
                unfocusedLabelColor = AdminSoftGray,
                focusedContainerColor = AdminCardNavy,
                unfocusedContainerColor = AdminCardNavy
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isPublishing = true
                guideViewModel.addGuide(context, placeName, selectedImageUris, accommodations) { success, message ->
                    isPublishing = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        placeName = ""
                        accommodations = ""
                        selectedImageUris = emptyList()
                    }
                }
            },
            enabled = !isPublishing,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = AdminAccentTeal,
                contentColor = AdminDeepNavy,
                disabledContainerColor = AdminAccentTeal.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            if (isPublishing) {
                CircularProgressIndicator(color = AdminDeepNavy, modifier = Modifier.size(28.dp))
            } else {
                Text(
                    text = "Publish Travel Guide",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AdminGuideList(guideViewModel: GuideViewModel) {
    val allGuides by guideViewModel.allGuides.observeAsState(initial = emptyList())
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        guideViewModel.getAllGuides()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (allGuides.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(R.drawable.outline_map_pin_review_24), null, tint = AdminSoftGray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No published guides", color = AdminSoftGray, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(allGuides) { guide ->
                    AdminGuideCard(guide = guide, onDelete = {
                        guideViewModel.deleteGuide(guide.guideId) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun AdminGuideCard(guide: GuideModel, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { guide.imageUrls.size })

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AdminCardNavy,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Remove Guide?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete the guide for '${guide.placeName}'.", color = AdminSoftGray) },
            confirmButton = {
                Button(
                    onClick = { 
                        onDelete()
                        showDeleteDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAlertRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardNavy)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AdminAccentTeal.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AdminAccentTeal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = guide.placeName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.background(AdminAlertRed.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AdminAlertRed, modifier = Modifier.size(20.dp))
                }
            }

            // High-Quality Pager
            if (guide.imageUrls.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Image(
                            painter = rememberAsyncImagePainter(guide.imageUrls[page]),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Glassmorphism Indicators
                    if (guide.imageUrls.size > 1) {
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(guide.imageUrls.size) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) AdminAccentTeal else Color.White.copy(alpha = 0.4f)
                                    val size = if (pagerState.currentPage == iteration) 8.dp else 6.dp
                                    Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(size)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Details Section
            if (guide.accommodations.isNotEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "ACCOMMODATIONS", color = AdminAccentTeal, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = guide.accommodations,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUsersList(userViewModel: UserViewModel) {
    val allUsers by userViewModel.allUsers.observeAsState(initial = emptyList())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("ACTIVE", "BANNED")

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
            divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    unselectedContentColor = AdminSoftGray
                )
            }
        }

        val filteredUsers = if (selectedTabIndex == 0) allUsers else emptyList<UserModel>()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
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
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = listOf(AdminAccentTeal, Color(0xFF3B82F6)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
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
                        Icon(Icons.Default.Email, null, tint = AdminAccentTeal, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(user.email, color = AdminSoftGray, fontSize = 13.sp)
                    }
                }
            }
            
            IconButton(
                onClick = { 
                    val action = if (isBannedView) "Unban" else "Ban"
                    Toast.makeText(context, "$action user logic here", Toast.LENGTH_SHORT).show() 
                },
                modifier = Modifier.background(if (isBannedView) AdminAccentTeal.copy(alpha = 0.1f) else AdminAlertRed.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isBannedView) Icons.Default.CheckCircle else Icons.Default.Block,
                    contentDescription = null,
                    tint = if (isBannedView) AdminAccentTeal else AdminAlertRed,
                    modifier = Modifier.size(22.dp)
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
            shape = RoundedCornerShape(24.dp),
            title = { Text("Delete User Post", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Provide a reason for removing this post.", color = AdminSoftGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deleteReason,
                        onValueChange = { deleteReason = it },
                        label = { Text("Violation Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AdminAccentTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = AdminDeepNavy,
                            unfocusedContainerColor = AdminDeepNavy
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteReason.isNotBlank()) {
                            postViewModel.deletePost(post.postId, post.userId, deleteReason) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) showDeleteDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Please provide a reason", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAlertRed)
                ) {
                    Text("Remove Post", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Dismiss", color = Color.White)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardNavy)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(AdminAccentTeal, Color(0xFF3B82F6)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = (author?.fullName?.take(1) ?: "T").uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = author?.fullName ?: "Traveler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.background(AdminAlertRed.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = AdminAlertRed, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (post.caption.isNotEmpty()) {
                Text(text = post.caption, color = Color.White, fontSize = 15.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (post.location.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = AdminAccentTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = post.location, color = AdminSoftGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (post.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)),
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
