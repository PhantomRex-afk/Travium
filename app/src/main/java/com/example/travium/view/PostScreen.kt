package com.example.travium.view

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.MakePostModel
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.viewmodel.MakePostViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakePostBody(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
){
    val context = LocalContext.current
    val activity = context as Activity
    val makePostViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }
    val coroutineScope = rememberCoroutineScope()

    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TravelDeepNavy,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New Post",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TravelDeepNavy
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Image Selector Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .clickable { onPickImage() },
                colors = CardDefaults.cardColors(containerColor = TravelCardNavy)
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(TravelCardNavy, TravelDeepNavy)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.image),
                                contentDescription = "Add Image",
                                tint = TravelAccentTeal,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Tap to add a photo",
                                color = TravelSoftGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caption Field
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TravelAccentTeal,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = TravelAccentTeal,
                    unfocusedLabelColor = TravelSoftGray,
                    cursorColor = TravelAccentTeal
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location Field
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Add Location") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TravelAccentTeal,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = TravelAccentTeal,
                    unfocusedLabelColor = TravelSoftGray,
                    cursorColor = TravelAccentTeal
                ),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_map_pin_review_24),
                        contentDescription = null,
                        tint = TravelAccentTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Post Button
            Button(
                onClick = {
                    if (selectedImageUri == null && caption.isBlank()) {
                        Toast.makeText(context, "Please add a photo or a caption", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    if (userId.isEmpty()) {
                        Toast.makeText(context, "Please log in to post", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        if (selectedImageUri != null) {
                            makePostViewModel.uploadImage(context, selectedImageUri) { imageUrl ->
                                if (imageUrl != null) {
                                    val post = MakePostModel(
                                        userId = userId,
                                        caption = caption,
                                        location = location,
                                        imageUrl = imageUrl
                                    )
                                    makePostViewModel.createPost(post) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) activity.finish()
                                    }
                                } else {
                                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val post = MakePostModel(userId = userId, caption = caption, location = location)
                            makePostViewModel.createPost(post) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) activity.finish()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TravelAccentTeal,
                    contentColor = TravelDeepNavy
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Share Post",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
