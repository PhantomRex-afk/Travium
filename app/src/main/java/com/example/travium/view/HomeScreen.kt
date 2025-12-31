package com.example.travium.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travium.model.MakePostModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.viewmodel.MakePostViewModel

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
            PostCard(post)
        }
    }
}

@Composable
fun PostCard(post: MakePostModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (post.caption.isNotEmpty()) {
                Text(text = post.caption)
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (post.location.isNotEmpty()) {
                Text(text = post.location)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreviewer(){
    HomeScreenBody()
}
