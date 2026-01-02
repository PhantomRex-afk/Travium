package com.example.travium.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.MakePostModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.viewmodel.MakePostViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreenBody() {
    val postViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }

    val allPosts by postViewModel.allPosts.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        postViewModel.getAllPosts()
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        items(allPosts) { post ->
            PostCard(post = post, postViewModel = postViewModel)
        }
    }
}

@Composable
fun PostCard(post: MakePostModel, postViewModel: MakePostViewModel) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (post.caption.isNotEmpty()) {
                Text(text = post.caption, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (post.location.isNotEmpty()) {
                Text(text = post.location, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (post.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { 
                    if (currentUserId.isNotEmpty()) {
                        postViewModel.likePost(post.postId, currentUserId) { success ->
                            if (!success) {
                                Toast.makeText(context, "Failed to like post", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "You must be logged in to like posts", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "Like",
                        tint = if (post.likes.contains(currentUserId)) Color.Red else Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { /*TODO: Handle comment*/ }) {
                    Icon(painter = painterResource(id = R.drawable.comment), contentDescription = "Comment")
                }
            }
            Text(text = "${post.likes.size} likes")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreviewer(){
    HomeScreenBody()
}
