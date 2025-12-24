package com.example.travium

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.UserViewModel

class ForgetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userRepo = UserRepoImpl()
            val userViewModel = UserViewModel(userRepo)
            ForgetPasswordBody(userViewModel)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetPasswordBody(viewModel: UserViewModel? = null) {

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF4ECDC4)
    val cardBackground = Color(0xCC1A1A1A)
    val textFieldBg = Color(0x33FFFFFF)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bglogin),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(4.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color(0xFF023E8A).copy(alpha = 0.5f)
                    )
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Forgot Password?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Enter your email and we’ll send a reset link",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = loginTextFieldColors(primaryColor, textFieldBg),
                        singleLine = true
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(primaryColor, secondaryColor)
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { 
                                if (email.isEmpty()) {
                                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                isLoading = true
                                viewModel?.forgetPassword(email) { success, message ->
                                    isLoading = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        Log.i("ForgetPasswordActivity", "Password reset email sent successfully to $email")
                                        (context as? Activity)?.finish()
                                    } else {
                                        Log.e("ForgetPasswordActivity", "Password reset failed: $message")
                                    }
                                }
                             },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text(
                            "Send Reset Link",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewForgetPassword() {
    ForgetPasswordBody()
}
