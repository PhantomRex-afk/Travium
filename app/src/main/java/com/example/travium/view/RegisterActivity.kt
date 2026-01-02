package com.example.travium.view

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.R
import com.example.travium.model.UserModel
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userRepo = UserRepoImpl()
            val userViewModel = UserViewModel(userRepo)
            RegisterBody(userViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBody(viewModel: UserViewModel? = null) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var confirmVisibility by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("") } // Start empty for placeholder
    var customCountry by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val options = listOf("Nepal", "Japan", "India", "China", "USA", "Brazil", "Spain", "Other")

    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(yearRange = 1900..Calendar.getInstance().get(Calendar.YEAR))

    val cardBackground = Color(0xCC1A1A1A)
    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF4ECDC4)
    val textFieldBg = Color(0x33FFFFFF)

    Scaffold { padding ->
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                            selectedDate = sdf.format(it)
                        }
                    }) { Text("OK", color = primaryColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = primaryColor) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.signup_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 4.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
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
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(primaryColor.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, primaryColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Create Account",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = White)
                    )
                }

                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, placeholder = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = premiumTextFieldColors(primaryColor, textFieldBg), singleLine = true)
                        OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("Enter your email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = premiumTextFieldColors(primaryColor, textFieldBg), singleLine = true)
                        OutlinedTextField(value = password, onValueChange = { password = it }, placeholder = { Text("Create a password") }, visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { visibility = !visibility }) { Icon(painter = if (visibility) painterResource(R.drawable.baseline_visibility_24) else painterResource(R.drawable.baseline_visibility_off_24), "Toggle password visibility", tint = White.copy(alpha = 0.7f)) } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = premiumTextFieldColors(primaryColor, textFieldBg), singleLine = true)
                        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = { Text("Confirm Password") }, visualTransformation = if (confirmVisibility) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { confirmVisibility = !confirmVisibility }) { Icon(painter = if (confirmVisibility) painterResource(R.drawable.baseline_visibility_24) else painterResource(R.drawable.baseline_visibility_off_24), "Toggle password visibility", tint = White.copy(alpha = 0.7f)) } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = premiumTextFieldColors(primaryColor, textFieldBg), singleLine = true)

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Country Selector Fix
                            Box(modifier = Modifier.weight(1f).clickable { expanded = true }) {
                                OutlinedTextField(
                                    value = selectedOptionText,
                                    onValueChange = {},
                                    enabled = false, // Prevents focus consumption
                                    placeholder = { Text("Select Country") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = White,
                                        disabledContainerColor = textFieldBg,
                                        disabledBorderColor = Color.Transparent,
                                        disabledPlaceholderColor = White.copy(alpha = 0.6f),
                                        disabledTrailingIconColor = White.copy(alpha = 0.7f)
                                    ),
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "", tint = White.copy(alpha = 0.7f)) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                // Dropdown should be anchored here
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(cardBackground)) {
                                    options.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, color = White) },
                                            onClick = {
                                                selectedOptionText = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Birth Date Fix
                            Box(modifier = Modifier.weight(1f).clickable { showDatePicker = true }) {
                                OutlinedTextField(
                                    value = selectedDate,
                                    onValueChange = {},
                                    enabled = false, // Prevents focus consumption
                                    placeholder = { Text("Birth Date") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = White,
                                        disabledContainerColor = textFieldBg,
                                        disabledBorderColor = Color.Transparent,
                                        disabledPlaceholderColor = White.copy(alpha = 0.6f),
                                        disabledTrailingIconColor = White.copy(alpha = 0.7f)
                                    ),
                                    trailingIcon = { Icon(Icons.Filled.DateRange, "Select Date", tint = White.copy(alpha = 0.7f)) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        if (selectedOptionText == "Other") {
                            OutlinedTextField(value = customCountry, onValueChange = { customCountry = it }, placeholder = { Text("Enter your country") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = premiumTextFieldColors(primaryColor, textFieldBg), singleLine = true)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { terms = !terms }) {
                            CustomCheckbox(checked = terms)
                            Spacer(Modifier.width(12.dp))
                            Text("I agree to the terms and conditions", color = White.copy(alpha = 0.9f), fontSize = 14.sp)
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(brush = Brush.horizontalGradient(
                            listOf(primaryColor, secondaryColor)
                        )).clickable {
                            if (!terms) {
                                Toast.makeText(context, "Please agree to terms", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            if (password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            val finalCountry = if (selectedOptionText == "Other") customCountry else selectedOptionText
                            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || selectedOptionText.isEmpty() || (selectedOptionText == "Other" && customCountry.isEmpty())) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            isLoading = true
                            viewModel?.register(email, password) { success, userId, message ->
                                if (success) {
                                    val userModel = UserModel(userId = userId, email = email, fullName = fullName, dob = selectedDate, country = finalCountry)
                                    viewModel.addUserToDatabase(userId, userModel) { dbSuccess, dbMessage ->
                                        isLoading = false
                                        Toast.makeText(context, dbMessage, Toast.LENGTH_SHORT).show()
                                        if (dbSuccess) {
                                            context.startActivity(Intent(context, LoginActivity::class.java))
                                            (context as? Activity)?.finish()
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }, contentAlignment = Alignment.Center) {
                            if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                            else Text("Create Account", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White))
                        }
                    }
                }

                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(White.copy(alpha = 0.8f))) { append("Already have an account? ") }
                        withStyle(SpanStyle(secondaryColor, fontWeight = FontWeight.Bold)) { append("Sign In") }
                    },
                    modifier = Modifier.clickable { context.startActivity(Intent(context, LoginActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
fun CustomCheckbox(checked: Boolean) {
    val primaryColor = Color(0xFF6C63FF)
    Box(
        modifier = Modifier.size(20.dp).background(color = if (checked) primaryColor else Color.Transparent, shape = RoundedCornerShape(6.dp)).border(width = 2.dp, color = if (checked) primaryColor else White.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun premiumTextFieldColors(primaryColor: Color, textFieldBg: Color) = TextFieldDefaults.colors(
    focusedContainerColor = textFieldBg, unfocusedContainerColor = textFieldBg, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = White, unfocusedTextColor = White, cursorColor = primaryColor, focusedPlaceholderColor = White.copy(alpha = 0.4f), unfocusedPlaceholderColor = White.copy(alpha = 0.6f)
)

@Preview(showBackground = true)
@Composable
fun PreviewRegister() {
    RegisterBody()
}
