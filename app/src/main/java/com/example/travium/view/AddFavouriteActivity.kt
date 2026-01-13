package com.example.travium.view

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.travium.model.FavouritePlace
import com.example.travium.repository.FavouriteRepositoryImpl
import com.example.travium.view.ui.theme.TraviumTheme
import com.example.travium.viewmodel.FavouriteViewModel
import com.example.travium.viewmodel.FavouriteViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AddFavouriteActivity : ComponentActivity() {

    private val viewModel: FavouriteViewModel by viewModels {
        FavouriteViewModelFactory(FavouriteRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TraviumTheme {
                val existingId = intent.getStringExtra("id")
                val existingName = intent.getStringExtra("name") ?: ""
                val existingDescription = intent.getStringExtra("description") ?: ""
                val existingImageUrl = intent.getStringExtra("imageUrl")

                AddFavouriteScreen(
                    viewModel = viewModel,
                    initialId = existingId,
                    initialName = existingName,
                    initialDescription = existingDescription,
                    initialImageUrl = existingImageUrl
                ) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavouriteScreen(
    modifier: Modifier = Modifier, 
    viewModel: FavouriteViewModel, 
    initialId: String? = null,
    initialName: String = "",
    initialDescription: String = "",
    initialImageUrl: String? = null,
    onSave: () -> Unit = {}
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    val scaffoldBg = Color(0xFFF7F7F7)
    val cardBg = Color.White
    val accentColor = Color(0xFF007AFF)
    val textColor = Color.Black
    val secondaryTextColor = Color.Gray

    Scaffold(
        containerColor = scaffoldBg,
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (initialId == null) "Add a New Memory" else "Edit Memory", color = textColor) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = accentColor)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val painter = if (imageUri != null) {
                            rememberAsyncImagePainter(imageUri)
                        } else if (initialImageUrl != null) {
                            rememberAsyncImagePainter(initialImageUrl)
                        } else {
                            null
                        }

                        painter?.let {
                            Image(
                                painter = it,
                                contentDescription = "Selected image",
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White)
                        ) {
                            Text("Select Image")
                        }

                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name", color = secondaryTextColor) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                cursorColor = accentColor,
                                focusedIndicatorColor = accentColor,
                                unfocusedIndicatorColor = secondaryTextColor.copy(alpha = 0.5f),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            textStyle = TextStyle(color = textColor)
                        )

                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description", color = secondaryTextColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                cursorColor = accentColor,
                                focusedIndicatorColor = accentColor,
                                unfocusedIndicatorColor = secondaryTextColor.copy(alpha = 0.5f),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            textStyle = TextStyle(color = textColor)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val user = Firebase.auth.currentUser
                                if (user != null) {
                                    val database = Firebase.database
                                    val myRef = database.getReference("users").child(user.uid).child("favourites")
                                    val favouriteId = initialId ?: myRef.push().key

                                    if (imageUri != null) {
                                        isUploading = true
                                        viewModel.uploadImage(context, imageUri!!) { imageUrl ->
                                            if (imageUrl != null && favouriteId != null) {
                                                val favouritePlace = FavouritePlace(favouriteId, name, description, imageUrl)
                                                myRef.child(favouriteId).setValue(favouritePlace).addOnSuccessListener {
                                                    isUploading = false
                                                    onSave()
                                                }
                                            }
                                        }
                                    } else {
                                        if (favouriteId != null) {
                                            val favouritePlace = FavouritePlace(favouriteId, name, description, initialImageUrl)
                                            myRef.child(favouriteId).setValue(favouritePlace).addOnSuccessListener {
                                                onSave()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White)
                        ) {
                            Text(if (initialId == null) "Save Memory" else "Update Memory")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddFavouriteScreenPreview() {
    TraviumTheme {
        // AddFavouriteScreen(viewModel = FavouriteViewModel(FavouriteRepositoryImpl()))
    }
}
