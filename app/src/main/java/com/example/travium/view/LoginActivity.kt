package com.example.travium.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.R
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.UserViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userRepo = UserRepoImpl()
            val userViewModel = UserViewModel(userRepo)
            LoginBody(userViewModel)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBody(viewModel: UserViewModel? = null) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var adminPasswordVisibility by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("User") }

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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Login",
                    fontSize = 33.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Discover new destinations, hidden gems & unforgettable experiences - at your fingertips",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBackground)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "User",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = if (selectedRole == "User") FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .background(if (selectedRole == "User") primaryColor else Color.Transparent)
                        .clickable { selectedRole = "User" }
                        .padding(horizontal = 40.dp, vertical = 12.dp)
                )
                Divider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                )
                Text(
                    "Admin",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = if (selectedRole == "Admin") FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .background(if (selectedRole == "Admin") primaryColor else Color.Transparent)
                        .clickable { selectedRole = "Admin" }
                        .padding(horizontal = 40.dp, vertical = 12.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = loginTextFieldColors(primaryColor, textFieldBg),
                        singleLine = true
                    )

                    AnimatedVisibility(visible = selectedRole == "User") {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Password") },
                            visualTransformation =
                            if (visibility) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { visibility = !visibility }) {
                                    Icon(
                                        painter = painterResource(
                                            if (visibility)
                                                R.drawable.baseline_visibility_24
                                            else
                                                R.drawable.baseline_visibility_off_24
                                        ),
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = loginTextFieldColors(primaryColor, textFieldBg),
                            singleLine = true
                        )
                    }

                    AnimatedVisibility(
                        visible = selectedRole == "Admin",
                        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(tween(300)),
                        exit = slideOutVertically(animationSpec = tween(300)) { it } + fadeOut(tween(300))
                    ) {
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            placeholder = { Text("Admin Password") },
                            visualTransformation = if (adminPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { adminPasswordVisibility = !adminPasswordVisibility }) {
                                    Icon(
                                        painter = painterResource(
                                            if (adminPasswordVisibility)
                                                R.drawable.baseline_visibility_24
                                            else
                                                R.drawable.baseline_visibility_off_24
                                        ),
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = loginTextFieldColors(primaryColor, textFieldBg),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomCheckbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = !rememberMe }
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Remember Me",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            modifier = Modifier.clickable {
                                rememberMe = !rememberMe
                            }
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            "Forgot Password?",
                            color = secondaryColor,
                            fontSize = 15.sp,
                            modifier = Modifier.clickable {
                                context.startActivity(
                                    Intent(context, ForgetPasswordActivity::class.java)
                                )
                            }
                        )

                    }
                    Spacer(Modifier.height(15.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(primaryColor, secondaryColor)
                                )
                            )
                            .clickable {
                                if (selectedRole == "Admin") {
                                    if (email.isEmpty() || adminPassword.isEmpty()) {
                                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }
                                    if (email == "admin@travium.com" && adminPassword == "admin123") {
                                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                        context.startActivity(Intent(context, AdminDashboardActivity::class.java))
                                    } else {
                                        Toast.makeText(context, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    if (email.isEmpty() || password.isEmpty()) {
                                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }
                                    isLoading = true
                                    viewModel?.login(email, password) { success, message ->
                                        isLoading = false
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            context.startActivity(Intent(context, HomePageActivity::class.java))
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading && selectedRole == "User") CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text(
                            "Log In",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(Color.White.copy(alpha = 0.8f))) {
                        append("Don’t have an account? ")
                    }
                    withStyle(
                        SpanStyle(
                            color = secondaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Sign Up")
                    }
                },
                fontSize = 18.sp,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                }
            )
        }
    }
}
@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val primaryColor = Color(0xFF6C63FF)

    Box(
        modifier = Modifier
            .size(20.dp)
            .clickable { onCheckedChange() }
            .background(
                color = if (checked) primaryColor else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 2.dp,
                color = if (checked) primaryColor else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
@Composable
fun loginTextFieldColors(
    primaryColor: Color,
    textFieldBg: Color
) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = textFieldBg,
    unfocusedContainerColor = textFieldBg,
    focusedBorderColor = primaryColor,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = primaryColor,
    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
)

@Preview(showBackground = true)
@Composable
fun PreviewLogin() {
    LoginBody()
}
