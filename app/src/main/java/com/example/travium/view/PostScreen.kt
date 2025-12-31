package com.example.travium.view

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakePostBody(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
){

    val context = LocalContext.current
    val activity = context as Activity
    val makePostViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }


    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Create a Post", style = TextStyle(
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                modifier = Modifier.height(70.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Gray,
                ),

            )
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding) // Use padding provided by Scaffold
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {

                Spacer( modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .height(300.dp)
                        .fillMaxWidth()
                        .clickable { onPickImage() } // Make the card clickable
                ) {
                    if (selectedImageUri != null) {
                        // If an image is selected, display it
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Otherwise, show the placeholder icon
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.image),
                                contentDescription = "Add Image",
                                modifier = Modifier.size(60.dp)
                            )
                            Text("Add Image")
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)
                ) {
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Caption") },
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)
                ){
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // This needs to be updated to handle the image upload
                        val post = MakePostModel(caption = caption, location = location)
                        makePostViewModel.createPost(post) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                        activity.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 15.dp)
                ) {
                    Text("Post", style = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold
                    ))
                }
                Spacer(modifier = Modifier.height(25.dp))
            }
        }
    }
