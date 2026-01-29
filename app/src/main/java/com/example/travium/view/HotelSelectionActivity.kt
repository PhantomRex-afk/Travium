package com.example.travium.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travium.viewmodel.HotelViewModel
import kotlin.math.roundToInt

class HotelSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HotelSelectionScreen(
                onHotelSelected = { hotel, roomType ->
                    val intent = Intent(this, CreateBookingActivity::class.java).apply {
                        putExtra("hotelId", hotel.hotelId)
                        putExtra("hotelName", hotel.hotelName)
                        putExtra("hotelOwnerId", hotel.hotelOwnerId)
                        putExtra("roomType", roomType.name)
                        putExtra("pricePerNight", roomType.pricePerNight)
                        putExtra("maxGuests", roomType.maxGuests)
                        putExtra("maxRooms", roomType.maxRooms)
                        putExtra("availableRooms", roomType.availableRooms)
                    }
                    startActivity(intent)
                },
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelSelectionScreen(
    onHotelSelected: (com.example.travium.model.HotelModel, com.example.travium.model.RoomType) -> Unit,
    onBack: () -> Unit
) {
    // FIX: Initialize ViewModel properly
    val viewModel: HotelViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    // State for search
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var selectedGuests by remember { mutableIntStateOf(1) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }

    // State for room type selection
    var showRoomTypesDialog by remember { mutableStateOf(false) }
    var selectedHotel by remember { mutableStateOf<com.example.travium.model.HotelModel?>(null) }

    // Fetch hotels on initial load
    LaunchedEffect(key1 = Unit) {
        viewModel.getAllHotels()
    }

    // Get the hotels list from ViewModel - FIX: Use value property
    val hotels = viewModel.hotels.value ?: emptyList()
    val uiState = viewModel.uiState.value
    val searchResults = viewModel.searchResults.value ?: emptyList()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Hotel & Room",
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
                .background(Color(0xFF0A1A2F))
        ) {
            // Search and Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A2A3F)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search hotels...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // City Filter
                        OutlinedTextField(
                            value = selectedCity,
                            onValueChange = { selectedCity = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("City") },
                            placeholder = { Text("Any") },
                            colors = textFieldColors
                        )

                        // Guests Filter
                        OutlinedTextField(
                            value = selectedGuests.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { guests ->
                                    if (guests > 0) selectedGuests = guests
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Guests") },
                            colors = textFieldColors
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Min Price
                        OutlinedTextField(
                            value = minPrice,
                            onValueChange = { minPrice = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Min Price") },
                            placeholder = { Text("0") },
                            colors = textFieldColors
                        )

                        // Max Price
                        OutlinedTextField(
                            value = maxPrice,
                            onValueChange = { maxPrice = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Max Price") },
                            placeholder = { Text("500") },
                            colors = textFieldColors
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Apply Filters Button
                    Button(
                        onClick = {
                            val minPriceValue = minPrice.toDoubleOrNull()
                            val maxPriceValue = maxPrice.toDoubleOrNull()

                            viewModel.searchHotels(
                                city = selectedCity.ifEmpty { null },
                                country = null,
                                minPrice = minPriceValue,
                                maxPrice = maxPriceValue,
                                guests = selectedGuests
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B4D8),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Apply Filters", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Hotels List
            if (uiState is com.example.travium.viewmodel.HotelUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00B4D8))
                }
            } else {
                val displayedHotels = if (searchResults.isNotEmpty()) {
                    searchResults
                } else {
                    hotels
                }

                if (displayedHotels.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchResults.isEmpty()) {
                                "No hotels available"
                            } else {
                                "No hotels match your filters"
                            },
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayedHotels) { hotel ->
                            HotelCard(
                                hotel = hotel,
                                onSelect = { selectedHotel = hotel; showRoomTypesDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // Room Type Selection Dialog
    if (showRoomTypesDialog && selectedHotel != null) {
        AlertDialog(
            onDismissRequest = { showRoomTypesDialog = false },
            title = {
                Text(
                    "Select Room Type at ${selectedHotel!!.hotelName}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn {
                    items(selectedHotel!!.roomTypes) { roomType ->
                        RoomTypeCard(
                            roomType = roomType,
                            onSelect = {
                                onHotelSelected(selectedHotel!!, roomType)
                                showRoomTypesDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoomTypesDialog = false }) {
                    Text("Cancel", color = Color(0xFF00B4D8))
                }
            },
            containerColor = Color(0xFF1A2A3F),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

@Composable
fun HotelCard(
    hotel: com.example.travium.model.HotelModel,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2A3F)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Hotel Image
            if (hotel.images.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(hotel.images.first()),
                    contentDescription = hotel.hotelName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Hotel Name and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hotel.hotelName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hotel.rating.toString(),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " (${hotel.totalReviews})",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${hotel.city}, ${hotel.country}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price Range
            val minRoomPrice = hotel.roomTypes.minOfOrNull { it.pricePerNight } ?: 0.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "From",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$${minRoomPrice.roundToInt()} / night",
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Available Room Types
                Text(
                    text = "${hotel.roomTypes.size} room types",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amenities Preview
            if (hotel.amenities.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Amenities",
                        tint = Color(0xFF00B4D8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hotel.amenities.take(3).joinToString(" • "),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun RoomTypeCard(
    roomType: com.example.travium.model.RoomType,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A1A2F)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = roomType.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "$${roomType.pricePerNight} / night",
                    color = Color(0xFF00B4D8),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Capacity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Capacity",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${roomType.maxGuests} guests max",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Availability
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MeetingRoom,
                    contentDescription = "Availability",
                    tint = if (roomType.availableRooms > 0) Color(0xFF00B4D8) else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (roomType.availableRooms > 0) {
                        "${roomType.availableRooms} rooms available"
                    } else {
                        "Sold out"
                    },
                    color = if (roomType.availableRooms > 0) Color.Gray else Color.Red,
                    fontSize = 14.sp
                )
            }

            if (roomType.amenities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Amenities: ${roomType.amenities.take(3).joinToString(", ")}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}