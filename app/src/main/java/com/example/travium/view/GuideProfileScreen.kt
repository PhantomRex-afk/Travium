package com.example.travium.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.UserModel
import com.example.travium.repository.UserRepoImpl
import com.example.travium.view.ui.theme.TraviumTheme
import com.example.travium.viewmodel.GuideViewModel

// Color Palette for consistency
private val ProfileDeepNavy = Color(0xFF0F172A)
private val ProfileCardNavy = Color(0xFF1E293B)
private val ProfileAccentTeal = Color(0xFF2DD4BF)
private val ProfileAccentPurple = Color(0xFF6C63FF)
private val ProfileSoftGray = Color(0xFF94A3B8)

class GuideProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val guideId = intent.getStringExtra("GUIDE_ID") ?: ""

        setContent {
            TraviumTheme {
                val viewModel: GuideViewModel = viewModel {
                    GuideViewModel(UserRepoImpl())
                }
                
                val user by viewModel.userProfile.collectAsState()
                val isLoading by viewModel.loading.collectAsState()

                LaunchedEffect(guideId) {
                    if (guideId.isNotEmpty()) {
                        viewModel.fetchGuideProfile(guideId)
                    }
                }

                GuideProfileContent(
                    user = user,
                    isLoading = isLoading,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideProfileContent(
    user: UserModel?,
    isLoading: Boolean,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = ProfileDeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Guide Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share Logic */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfileDeepNavy)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfileAccentTeal)
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = ProfileSoftGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Guide details not found", color = ProfileSoftGray, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = ProfileAccentPurple)
                    ) { Text("Go Back", color = Color.White) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            androidx.compose.foundation.Image(
                                painter = rememberAsyncImagePainter(
                                    model = user.profileImageUrl.ifEmpty { R.drawable.profile }
                                ),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, ProfileAccentPurple.copy(alpha = 0.5f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                color = ProfileAccentTeal,
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp).border(2.dp, ProfileDeepNavy, CircleShape)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Verified", tint = ProfileDeepNavy, modifier = Modifier.padding(6.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(user.fullName, style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                        Text(user.specialties.ifEmpty { "General Guide" }, style = TextStyle(color = ProfileAccentTeal, fontWeight = FontWeight.Medium, fontSize = 16.sp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = ProfileSoftGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(user.location.ifEmpty { user.country }, color = ProfileSoftGray, fontSize = 14.sp)
                        }
                    }
                }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStatItem(user.yearsOfExperience.ifEmpty { "0" }, "Years Exp.")
                        VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = ProfileSoftGray.copy(alpha = 0.3f))
                        ProfileStatItem("New", "Rating")
                        VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = ProfileSoftGray.copy(alpha = 0.3f))
                        ProfileStatItem("0", "Trips")
                    }
                }

                // Bio Section
                item {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("About Me", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.bio.ifEmpty { "No biography provided." },
                            style = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, color = ProfileSoftGray)
                        )
                    }
                }

                // Contact Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ProfileCardNavy)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Professional Details", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileDetailRow(Icons.Default.Email, "Email", user.email)
                            ProfileDetailRow(Icons.Default.Phone, "Phone", user.phoneNumber.ifEmpty { "Not provided" })
                            ProfileDetailRow(Icons.Default.Person, "Gender", user.gender)
                            ProfileDetailRow(Icons.Default.Cake, "Birth Date", user.dob)
                        }
                    }
                }
            }
        }
    }

    // Floating Bottom Buttons
    if (user != null && !isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ProfileCardNavy,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Message Intent */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ProfileAccentTeal)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = ProfileAccentTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Message", fontWeight = FontWeight.Bold, color = ProfileAccentTeal)
                    }
                    Button(
                        onClick = { /* Hire Intent */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfileAccentPurple)
                    ) {
                        Text("Hire Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ProfileAccentTeal))
        Text(label, style = TextStyle(fontSize = 12.sp, color = ProfileSoftGray))
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(ProfileAccentPurple.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = ProfileAccentPurple)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = ProfileSoftGray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GuideProfilePremiumPreview() {
    TraviumTheme {
        GuideProfileContent(
            user = UserModel(
                fullName = "Anish Subedi",
                location = "Pokhara, Kaski",
                specialties = "Trekking & Cultural Tours",
                yearsOfExperience = "8",
                bio = "Professional guide based in Pokhara with over 8 years of experience. Specializing in Annapurna region treks and local cultural tours.",
                email = "anish.guide@example.com",
                phoneNumber = "+977 9800000000",
                dob = "12/05/1995",
                gender = "Male",
                isGuide = true
            ),
            isLoading = false,
            onBack = {}
        )
    }
}
