package com.example.travium.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travium.model.BookingModel
import com.example.travium.repository.BookingRepoImpl
import com.example.travium.repository.HotelRepoImpl
import com.example.travium.viewmodel.BookingViewModel
import com.example.travium.viewmodel.HotelViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class CreateBookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hotelId = intent.getStringExtra("hotelId") ?: ""
        val roomType = intent.getStringExtra("roomType") ?: "Standard"
        val pricePerNight = intent.getDoubleExtra("pricePerNight", 0.0)
        val maxGuests = intent.getIntExtra("maxGuests", 2)
        val maxRooms = intent.getIntExtra("maxRooms", 1)
        val availableRooms = intent.getIntExtra("availableRooms", 0)

        // Create ViewModel Factory
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(BookingViewModel::class.java) -> {
                        BookingViewModel(BookingRepoImpl()) as T
                    }
                    modelClass.isAssignableFrom(HotelViewModel::class.java) -> {
                        HotelViewModel(HotelRepoImpl()) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        setContent {
            CreateBookingScreen(
                hotelId = hotelId,
                roomType = roomType,
                pricePerNight = pricePerNight,
                maxGuests = maxGuests,
                maxRooms = maxRooms,
                availableRooms = availableRooms,
                onBookingCreated = { bookingId ->
                    Toast.makeText(this, "Booking created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onBack = { finish() },
                viewModelFactory = viewModelFactory
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    hotelId: String,
    roomType: String,
    pricePerNight: Double,
    maxGuests: Int,
    maxRooms: Int,
    availableRooms: Int,
    onBookingCreated: (String) -> Unit,
    onBack: () -> Unit,
    viewModelFactory: ViewModelProvider.Factory
) {
    val context = LocalContext.current

    // Use the factory with viewModel
    val bookingViewModel: BookingViewModel = viewModel(factory = viewModelFactory)
    val hotelViewModel: HotelViewModel = viewModel(factory = viewModelFactory)

    // Current user info
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Guest"
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    // State variables
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var numberOfGuests by remember { mutableIntStateOf(1) }
    var numberOfRooms by remember { mutableIntStateOf(1) }
    var specialRequests by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }

    // Hotel information
    var hotelInfo by remember { mutableStateOf<com.example.travium.model.HotelModel?>(null) }

    // Date pickers
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Calculate total price
    val nights = calculateNights(checkInDate, checkOutDate)
    val totalPrice = nights * numberOfRooms * pricePerNight

    // Get UI state
    val bookingUiState = bookingViewModel.uiState.value

    // Define common text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF00B4D8),
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color.Gray,
        unfocusedLabelColor = Color.Gray,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedLeadingIconColor = Color.Gray,
        unfocusedLeadingIconColor = Color.Gray,
        focusedTrailingIconColor = Color.Gray,
        unfocusedTrailingIconColor = Color.Gray,
        cursorColor = Color(0xFF00B4D8),
        focusedContainerColor = Color(0xFF0A1A2F),
        unfocusedContainerColor = Color(0xFF0A1A2F),
        disabledContainerColor = Color(0xFF0A1A2F),
        errorContainerColor = Color(0xFF0A1A2F),
    )

    // Define disabled text field colors
    val disabledTextFieldColors = OutlinedTextFieldDefaults.colors(
        disabledTextColor = Color.White,
        disabledBorderColor = Color.Gray,
        disabledLabelColor = Color.Gray,
        disabledPlaceholderColor = Color.Gray,
        disabledLeadingIconColor = Color.Gray,
        disabledTrailingIconColor = Color.Gray,
        disabledContainerColor = Color(0xFF0A1A2F),
    )

    // Load hotel information
    LaunchedEffect(key1 = hotelId) {
        if (hotelId.isNotEmpty()) {
            hotelViewModel.getHotelById(hotelId)
        }
    }

    // Observe hotel information
    LaunchedEffect(key1 = hotelViewModel.selectedHotel) {
        hotelViewModel.selectedHotel.value?.let { hotel ->
            hotelInfo = hotel
        }
    }

    // Listen for UI state changes
    LaunchedEffect(key1 = bookingUiState) {
        when (bookingUiState) {
            is com.example.travium.viewmodel.BookingUiState.BookingCreated -> {
                val bookingId = bookingUiState.booking.bookingId
                onBookingCreated(bookingId)
            }
            is com.example.travium.viewmodel.BookingUiState.Error -> {
                val error = bookingUiState.message
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Book ${hotelInfo?.hotelName ?: "Hotel"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A2A3F)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFF0A1A2F))
                .padding(16.dp)
        ) {
            // Hotel Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A2A3F)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = hotelInfo?.hotelName ?: "Loading...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    if (hotelInfo != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${hotelInfo!!.city}, ${hotelInfo!!.country}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Room Type: $roomType",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Price per night: $$pricePerNight",
                        color = Color(0xFF00B4D8),
                        fontSize = 14.sp
                    )

                    // Hotel Policies
                    if (hotelInfo?.policies?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hotel Policies:",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        hotelInfo!!.policies.take(2).forEach { policy ->
                            Text(
                                text = "• ${policy.title}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Guest Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A2A3F)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Guest Information",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = currentUserName,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                        enabled = false,
                        colors = disabledTextFieldColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = currentUserEmail,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        enabled = false,
                        colors = disabledTextFieldColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userPhone,
                        onValueChange = { userPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone Number") },
                        placeholder = { Text("Enter your phone number") },
                        colors = textFieldColors
                    )
                }
            }

            // Booking Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A2A3F)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Booking Details",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Check-in Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = checkInDate,
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            label = { Text("Check-in Date") },
                            placeholder = { Text("Select date") },
                            enabled = false,
                            colors = disabledTextFieldColors
                        )

                        IconButton(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        checkInDate = dateFormatter.format(calendar.time)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = Color(0xFF00B4D8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Check-out Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = checkOutDate,
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            label = { Text("Check-out Date") },
                            placeholder = { Text("Select date") },
                            enabled = false,
                            colors = disabledTextFieldColors
                        )

                        IconButton(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        checkOutDate = dateFormatter.format(calendar.time)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = Color(0xFF00B4D8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Number of Guests
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Guests:",
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (numberOfGuests > 1) numberOfGuests--
                            },
                            enabled = numberOfGuests > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF00B4D8))
                        }

                        Text(
                            text = "$numberOfGuests",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                if (numberOfGuests < maxGuests) numberOfGuests++
                            },
                            enabled = numberOfGuests < maxGuests
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF00B4D8))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Number of Rooms
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rooms:",
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (numberOfRooms > 1) numberOfRooms--
                            },
                            enabled = numberOfRooms > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF00B4D8))
                        }

                        Text(
                            text = "$numberOfRooms",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                if (numberOfRooms < maxRooms && numberOfRooms < availableRooms) numberOfRooms++
                            },
                            enabled = numberOfRooms < maxRooms && numberOfRooms < availableRooms
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF00B4D8))
                        }
                    }

                    // Availability warning
                    if (numberOfRooms > availableRooms) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Only $availableRooms rooms available",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Special Requests
                    OutlinedTextField(
                        value = specialRequests,
                        onValueChange = { specialRequests = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Special Requests (Optional)") },
                        placeholder = { Text("Any special requests?") },
                        maxLines = 3,
                        colors = textFieldColors
                    )
                }
            }

            // Price Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A2A3F)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Price Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$pricePerNight × $nights nights × $numberOfRooms rooms", color = Color.Gray)
                        Text("$${pricePerNight * nights * numberOfRooms}", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Taxes and Fees (example)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Taxes & Fees", color = Color.Gray)
                        Text("$${(totalPrice * 0.1).format(2)}", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "$${(totalPrice * 1.1).format(2)}",
                            color = Color(0xFF00B4D8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Book Now Button
            Button(
                onClick = {
                    // Validate inputs
                    if (checkInDate.isEmpty() || checkOutDate.isEmpty()) {
                        Toast.makeText(context, "Please select check-in and check-out dates", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (userPhone.isEmpty()) {
                        Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (numberOfRooms > availableRooms) {
                        Toast.makeText(context, "Not enough rooms available", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (nights <= 0) {
                        Toast.makeText(context, "Check-out date must be after check-in date", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Create booking
                    val booking = BookingModel(
                        hotelId = hotelId,
                        hotelName = hotelInfo?.hotelName ?: "Unknown Hotel",
                        hotelOwnerId = hotelInfo?.hotelOwnerId ?: "",
                        userId = currentUserId,
                        userName = currentUserName,
                        userEmail = currentUserEmail,
                        userPhone = userPhone,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate,
                        numberOfGuests = numberOfGuests,
                        numberOfRooms = numberOfRooms,
                        roomType = roomType,
                        totalPrice = totalPrice * 1.1, // Including taxes
                        specialRequests = specialRequests,
                        cancellationPolicy = hotelInfo?.policies?.find { it.title.contains("cancellation", ignoreCase = true) }?.description
                            ?: "Free cancellation up to 24 hours before check-in"
                    )

                    bookingViewModel.createBooking(booking)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B4D8),
                    contentColor = Color.White
                ),
                enabled = bookingUiState !is com.example.travium.viewmodel.BookingUiState.Loading &&
                        hotelInfo != null &&
                        numberOfRooms <= availableRooms
            ) {
                if (bookingUiState is com.example.travium.viewmodel.BookingUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Confirm Booking - $${(totalPrice * 1.1).format(2)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun calculateNights(checkInDate: String, checkOutDate: String): Int {
    if (checkInDate.isEmpty() || checkOutDate.isEmpty()) return 0

    return try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val checkIn = format.parse(checkInDate)
        val checkOut = format.parse(checkOutDate)

        if (checkIn != null && checkOut != null) {
            val diff = checkOut.time - checkIn.time
            val days = diff / (24 * 60 * 60 * 1000)
            days.toInt()
        } else {
            0
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

private fun Double.format(decimals: Int): String {
    return String.format("%.${decimals}f", this)
}