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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travium.R
import com.example.travium.repository.GuideRepoImpl
import com.example.travium.view.ui.theme.TraviumTheme
import com.example.travium.viewmodel.GuideViewModel
import java.text.SimpleDateFormat
import java.util.*

class GuideRegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraviumTheme {
                val viewModel: GuideViewModel = viewModel {
                    GuideViewModel(GuideRepoImpl())
                }
                
                val isLoading by viewModel.loading.collectAsState()
                val status by viewModel.status.collectAsState()
                
                GuideRegistrationScreen(
                    isLoading = isLoading,
                    status = status,
                    onRegister = { fullName, dob, gender, email, phone, location, experience, specialties, bio ->
                        viewModel.registerGuide(fullName, dob, gender, email, phone, location, experience, specialties, bio)
                    },
                    onSuccess = {
                        val intent = Intent(this, HomePageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    clearStatus = { viewModel.clearStatus() },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideRegistrationScreen(
    isLoading: Boolean,
    status: String?,
    onRegister: (String, String, String, String, String, String, String, String, String) -> Unit,
    onSuccess: () -> Unit,
    clearStatus: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Form states
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var specialties by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    // UI States
    var showValidationError by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(yearRange = 1900..Calendar.getInstance().get(Calendar.YEAR))
    
    val genders = listOf("Male", "Female", "Other")
    val districts = remember {
        listOf(
            "Achham", "Arghakhanchi", "Baglung", "Baitadi", "Bajhang", "Bajura", "Banke", "Bara", "Bardiya", "Bhaktapur",
            "Bhojpur", "Chitwan", "Dadeldhura", "Dailekh", "Dang", "Darchula", "Dhading", "Dhankuta", "Dhanusa", "Dolakha",
            "Dolpa", "Doti", "Gorkha", "Gulmi", "Humla", "Ilam", "Jajarkot", "Jhapa", "Jumla", "Kailali", "Kalikot",
            "Kanchanpur", "Kapilvastu", "Kaski", "Kathmandu", "Kavrepalanchok", "Khotang", "Lalitpur", "Lamjung", "Mahottari",
            "Makwanpur", "Manang", "Morang", "Mugu", "Mustang", "Myagdi", "Nawalpur", "Parasi", "Nuwakot", "Okhaldhunga",
            "Palpa", "Panchthar", "Parbat", "Parsa", "Pyuthan", "Ramechhap", "Rasuwa", "Rautahat", "Rolpa", "Rukum East",
            "Rukum West", "Rupandehi", "Salyan", "Sankhuwasabha", "Saptari", "Sarlahi", "Sindhuli", "Sindhupalchok", "Siraha",
            "Solukhumbu", "Sunsari", "Surkhet", "Syangja", "Tanahu", "Taplejung", "Terhathum", "Udayapur"
        ).sorted()
    }

    val filteredDistricts = remember(location) {
        if (location.isEmpty()) districts else districts.filter { it.contains(location, ignoreCase = true) }
    }

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("success", ignoreCase = true)) {
                onSuccess()
            }
            clearStatus()
        }
    }

    val cardBackground = Color(0xCC1A1A1A)
    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF4ECDC4)
    val textFieldBg = Color(0x33FFFFFF)

    if (showValidationError) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            containerColor = Color(0xFF1A1A1A),
            icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = primaryColor) },
            title = { Text("Missing Details", color = White, fontWeight = FontWeight.Bold) },
            text = { Text("Please ensure all fields are filled before submitting your application.", color = White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { showValidationError = false }) {
                    Text("OK", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold { padding ->
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                            dob = sdf.format(it)
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
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                    Text(
                        "Guide Registration",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = White),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Join as a Guide",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = White)
                        )
                        Text(
                            "Fill out the details below",
                            style = TextStyle(fontSize = 14.sp, color = White.copy(alpha = 0.7f))
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackground)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 24.dp)
                        ) {
                            item { GuideSectionHeader("Personal Info", primaryColor) }
                            
                            item {
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    placeholder = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = guidePremiumTextFieldColors(primaryColor, textFieldBg),
                                    singleLine = true
                                )
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f).clickable { showDatePicker = true }) {
                                        OutlinedTextField(
                                            value = dob,
                                            onValueChange = {},
                                            enabled = false,
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
                                    
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(
                                            value = gender,
                                            onValueChange = {},
                                            readOnly = true,
                                            placeholder = { Text("Gender") },
                                            modifier = Modifier.fillMaxWidth().clickable { genderExpanded = true },
                                            enabled = false,
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "", tint = White.copy(alpha = 0.7f)) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = White,
                                                disabledContainerColor = textFieldBg,
                                                disabledBorderColor = Color.Transparent,
                                                disabledPlaceholderColor = White.copy(alpha = 0.6f),
                                                disabledTrailingIconColor = White.copy(alpha = 0.7f)
                                            )
                                        )
                                        DropdownMenu(
                                            expanded = genderExpanded,
                                            onDismissRequest = { genderExpanded = false },
                                            modifier = Modifier.background(Color(0xFF1A1A1A))
                                        ) {
                                            genders.forEach { selection ->
                                                DropdownMenuItem(
                                                    text = { Text(selection, color = White) },
                                                    onClick = {
                                                        gender = selection
                                                        genderExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item { GuideSectionHeader("Contact Info", primaryColor) }

                            item {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("Email Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = guidePremiumTextFieldColors(primaryColor, textFieldBg),
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    placeholder = { Text("Phone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = guidePremiumTextFieldColors(primaryColor, textFieldBg),
                                    singleLine = true
                                )
                            }

                            item {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = location,
                                        onValueChange = { 
                                            location = it
                                            districtExpanded = true 
                                        },
                                        placeholder = { Text("District") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = { 
                                            IconButton(onClick = { districtExpanded = !districtExpanded }) {
                                                Icon(Icons.Default.ArrowDropDown, "", tint = White.copy(alpha = 0.7f))
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = guidePremiumTextFieldColors(primaryColor, textFieldBg)
                                    )
                                    if (filteredDistricts.isNotEmpty()) {
                                        DropdownMenu(
                                            expanded = districtExpanded,
                                            onDismissRequest = { districtExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.8f).background(Color(0xFF1A1A1A))
                                        ) {
                                            filteredDistricts.take(5).forEach { district ->
                                                DropdownMenuItem(
                                                    text = { Text(district, color = White) },
                                                    onClick = {
                                                        location = district
                                                        districtExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item { GuideSectionHeader("Professional Exp.", primaryColor) }

                            item {
                                OutlinedTextField(
                                    value = experience,
                                    onValueChange = { experience = it },
                                    placeholder = { Text("Years of Experience") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = guidePremiumTextFieldColors(primaryColor, textFieldBg),
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = specialties,
                                    onValueChange = { specialties = it },
                                    placeholder = { Text("Specialties (e.g. Trekking, History)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = guidePremiumTextFieldColors(primaryColor, textFieldBg),
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = bio,
                                    onValueChange = { bio = it },
                                    placeholder = { Text("Short Bio") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = guidePremiumTextFieldColors(primaryColor, textFieldBg),
                                    maxLines = 5
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Application Button - Outside the Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                            .clickable {
                                if (fullName.isBlank() || dob.isBlank() || gender.isBlank() || email.isBlank() || phone.isBlank() || location.isBlank() || experience.isBlank() || specialties.isBlank() || bio.isBlank()) {
                                    showValidationError = true
                                } else if (!isOver18(dob)) {
                                    Toast.makeText(context, "You must be 18 or older to register", Toast.LENGTH_SHORT).show()
                                } else {
                                    onRegister(fullName, dob, gender, email, phone, location, experience, specialties, bio)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        else Text("Submit Application", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In Button - Outside the Card
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(White.copy(alpha = 0.8f))) { append("Already have an account? ") }
                            withStyle(SpanStyle(secondaryColor, fontWeight = FontWeight.Bold)) { append("Sign In") }
                        },
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .clickable { 
                                val intent = Intent(context, HomePageActivity::class.java)
                                context.startActivity(intent)
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun GuideSectionHeader(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun guidePremiumTextFieldColors(primaryColor: Color, textFieldBg: Color) = TextFieldDefaults.colors(
    focusedContainerColor = textFieldBg,
    unfocusedContainerColor = textFieldBg,
    focusedIndicatorColor = primaryColor,
    unfocusedIndicatorColor = Color.Transparent,
    focusedTextColor = White,
    unfocusedTextColor = White,
    cursorColor = primaryColor,
    focusedPlaceholderColor = White.copy(alpha = 0.4f),
    unfocusedPlaceholderColor = White.copy(alpha = 0.6f)
)

fun validateForm(name: String, dob: String, email: String, phone: String, loc: String): Boolean {
    return name.isNotBlank() && dob.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && loc.isNotBlank()
}

fun isOver18(dob: String): Boolean {
    if (dob.isEmpty()) return false
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    val birthDate = try { sdf.parse(dob) } catch(e: Exception) { null } ?: return false
    val today = Calendar.getInstance()
    val birth = Calendar.getInstance().apply { time = birthDate }
    
    var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age >= 18
}

@Preview(showBackground = true)
@Composable
fun GuideRegistrationPreview() {
    TraviumTheme {
        GuideRegistrationScreen(
            isLoading = false,
            status = null,
            onRegister = { _, _, _, _, _, _, _, _, _ -> },
            onSuccess = {},
            clearStatus = {},
            onBack = {}
        )
    }
}
