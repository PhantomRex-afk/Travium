package com.example.travium.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.Comment
import com.example.travium.model.MakePostModel
import com.example.travium.model.UserModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenBody() {
    val postViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val allPosts by postViewModel.allPosts.observeAsState(initial = emptyList())
    
    var selectedPost by remember { mutableStateOf<MakePostModel?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        postViewModel.getAllPosts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(allPosts) { post ->
                PostCard(
                    post = post, 
                    postViewModel = postViewModel, 
                    userViewModel = userViewModel,
                    onCommentClick = {
                        selectedPost = post
                        isSheetOpen = true
                    }
                )
            }
        }

        if (isSheetOpen && selectedPost != null) {
            ModalBottomSheet(
                onDismissRequest = { isSheetOpen = false },
                sheetState = sheetState,
                modifier = Modifier.fillMaxHeight(0.8f)
            ) {
                CommentSection(
                    post = selectedPost!!, 
                    postViewModel = postViewModel, 
                    userViewModel = userViewModel
                )
            }
        }
    }
}

@Composable
fun PostAuthorHeader(userId: String, userViewModel: UserViewModel) {
    var user by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(userId) {
        userViewModel.getUserById(userId) { fetchedUser ->
            user = fetchedUser
        }
    }

    Text(text = user?.fullName ?: "Loading...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
}

@Composable
fun PostCard(
    post: MakePostModel, 
    postViewModel: MakePostViewModel, 
    userViewModel: UserViewModel,
    onCommentClick: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PostAuthorHeader(userId = post.userId, userViewModel = userViewModel)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (post.caption.isNotEmpty()) {
                Text(text = post.caption, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (post.location.isNotEmpty()) {
                Text(text = post.location, color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (post.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { 
                    if (currentUserId.isNotEmpty()) {
                        postViewModel.likePost(post.postId, currentUserId) { success ->
                            if (!success) {
                                Toast.makeText(context, "Failed to like post", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please log in to like", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "Like",
                        tint = if (post.likes.contains(currentUserId)) Color.Red else Color.Gray
                    )
                }
                Text(text = "${post.likes.size}")
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(onClick = onCommentClick) {
                    Icon(painter = painterResource(id = R.drawable.comment), contentDescription = "Comment")
                }
                Text(text = "${post.comments.size}")
            }
        }
    }
}

@Composable
fun CommentSection(post: MakePostModel, postViewModel: MakePostViewModel, userViewModel: UserViewModel) {
    var newCommentText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var currentUserProfile by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId) { user ->
                currentUserProfile = user
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Comments", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(post.comments) { comment ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = comment.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = comment.message, fontSize = 14.sp)
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add a comment...") }
            )
            IconButton(onClick = { 
                if (currentUserId.isNotEmpty() && newCommentText.isNotBlank()) {
                    val comment = Comment(
                        userId = currentUserId,
                        fullName = currentUserProfile?.fullName ?: "User",
                        message = newCommentText
                    )
                    postViewModel.addComment(post.postId, comment) { success ->
                        if (success) {
                            newCommentText = ""
                        }
                    }
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreviewer(){
    HomeScreenBody()
}
