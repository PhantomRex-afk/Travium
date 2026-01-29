package com.example.travium.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.travium.model.UserModel
import com.example.travium.repository.ProfileRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.ui.theme.TraviumTheme
import com.example.travium.viewmodel.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userRepo = UserRepoImpl()
            val userViewModel = UserViewModel(userRepo)
            TraviumTheme {
                EditProfileBody(userViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBody(viewModel: UserViewModel? = null) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val profileRepo = remember { ProfileRepoImpl(context) }

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val darkNavy = Color(0xFF000033)
    val midnightBlue = Color(0xFF003366)
    val cyanAccent = Color(0xFF00FFFF)

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    // Load current user data
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            val database = Firebase.database.getReference("users").child(currentUser.uid)
            database.get().addOnSuccessListener { snapshot ->
                name = snapshot.child("fullName").getValue(String::class.java) ?: ""
                username = snapshot.child("username").getValue(String::class.java) ?: ""
                bio = snapshot.child("bio").getValue(String::class.java) ?: ""
                dob = snapshot.child("dob").getValue(String::class.java) ?: ""
                country = snapshot.child("country").getValue(String::class.java) ?: ""
                profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
            }
        }
    }

    Scaffold(containerColor = darkNavy) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = cyanAccent)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(2.dp, cyanAccent),
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUri ?: profileImageUrl ?: R.drawable.blastoise
                            ),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .clickable {
                                    singlePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = midnightBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ModernEditField(label = "Full Name", value = name) { name = it }
                        Spacer(Modifier.height(12.dp))
                        ModernEditField(label = "Username", value = username) { username = it }
                        Spacer(Modifier.height(12.dp))
                        ModernEditField(label = "Bio", value = bio) { bio = it }
                        Spacer(Modifier.height(12.dp))
                        ModernEditField(label = "Country", value = country) { country = it }
                        Spacer(Modifier.height(12.dp))
                        ModernEditField(label = "Date of Birth", value = dob) { dob = it }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (currentUser == null) return@Button
                        isLoading = true

                        val updateProfile = { imageUrl: String? ->
                            val updatedUser = hashMapOf(
                                "fullName" to name,
                                "username" to username,
                                "bio" to bio,
                                "country" to country,
                                "dob" to dob
                            )
                            if (imageUrl != null) {
                                updatedUser["profileImageUrl"] = imageUrl
                            }

                            Firebase.database.getReference("users").child(currentUser.uid)
                                .updateChildren(updatedUser as Map<String, Any>)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                                        (context as? Activity)?.finish()
                                    } else {
                                        Toast.makeText(context, "Update Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }

                        if (imageUri != null) {
                            profileRepo.uploadProfileImage(imageUri!!) { success, imageUrl ->
                                if (success && imageUrl != null) {
                                    updateProfile(imageUrl)
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            updateProfile(null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cyanAccent,
                        contentColor = darkNavy
                    )
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ModernEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    val cyanAccent = Color(0xFF00FFFF)

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = Color.White),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = cyanAccent,
            focusedIndicatorColor = cyanAccent,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = cyanAccent
        )
    )
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    TraviumTheme {
        EditProfileBody()
    }
}