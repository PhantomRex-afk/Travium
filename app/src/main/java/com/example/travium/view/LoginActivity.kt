package com.example.travium.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var visibility by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(Modifier.height(16.dp))
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
                    ))
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .height(350.dp),
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
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
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
