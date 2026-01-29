// HotelSelectionActivity.kt (updated with working filters)
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travium.model.HotelModel
import com.example.travium.repository.HotelRepoImpl
import com.example.travium.viewmodel.HotelViewModel
import kotlin.math.roundToInt

class HotelSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HotelSelectionScreen(
                onHotelSelected = { hotel ->
                    // Since your HotelModel doesn't have roomTypes, we'll use default values
                    val intent = Intent(this, CreateBookingActivity::class.java).apply {
                        putExtra("hotelId", hotel.hotelId)
                        putExtra("roomType", "Standard Room") // Default room type
                        putExtra("pricePerNight", extractMinPrice(hotel.priceRange)) // Extract min price from range
                        putExtra("maxGuests", 2) // Default guests
                        putExtra("maxRooms", 5) // Default max rooms
                        putExtra("availableRooms", 5) // Default available rooms
                    }
                    startActivity(intent)
                },
                onBack = { finish() }
            )
        }
    }

    private fun extractMinPrice(priceRange: String): Double {
        return try {
            // Extract numbers from price range like "$100-$200" or "100-200"
            val numbers = priceRange.replace("[^0-9-]".toRegex(), "")
            val parts = numbers.split("-")
            if (parts.isNotEmpty()) parts[0].toDouble() else 100.0
        } catch (e: Exception) {
            100.0 // Default price
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelSelectionScreen(
    onHotelSelected: (HotelModel) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HotelViewModel = viewModel(
        factory = HotelViewModelFactory(HotelRepoImpl())
    )

    // State for search and filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedGuests by remember { mutableIntStateOf(1) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    // State for filtered hotels
    var filteredHotels by remember { mutableStateOf<List<HotelModel>>(emptyList()) }

    // State for room type selection
    var showHotelDetailsDialog by remember { mutableStateOf(false) }
    var selectedHotel by remember { mutableStateOf<HotelModel?>(null) }

    // Fetch hotels on initial load
    LaunchedEffect(key1 = Unit) {
        viewModel.getAllHotels()
    }

    // Get the hotels list from ViewModel
    val allHotels by viewModel.allHotels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Update filtered hotels when allHotels changes
    LaunchedEffect(allHotels) {
        filteredHotels = allHotels
    }

    // Define common text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF00B4D8),
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color.Gray,
        unfocusedLabelColor = Color.Gray,
        cursorColor = Color(0xFF00B4D8),
        focusedContainerColor = Color(0xFF0A1A2F),
        unfocusedContainerColor = Color(0xFF0A1A2F),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Hotel",
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
                actions = {
                    IconButton(
                        onClick = { showFilters = !showFilters }
                    ) {
                        Icon(
                            if (showFilters) Icons.Default.FilterAlt else Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (showFilters) Color(0xFF00B4D8) else Color.White
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
            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A2A3F)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            applyFilters(
                                allHotels = allHotels,
                                searchQuery = it,
                                location = selectedLocation,
                                guests = selectedGuests,
                                minPrice = minPrice,
                                maxPrice = maxPrice,
                                onResult = { filteredHotels = it }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Search hotels...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        },
                        colors = textFieldColors,
                        singleLine = true
                    )
                }
            }

            // Filters Section (Collapsible)
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A2A3F)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Location Filter
                        OutlinedTextField(
                            value = selectedLocation,
                            onValueChange = { selectedLocation = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Location", color = Color.Gray) },
                            placeholder = { Text("City or area", color = Color.Gray) },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray)
                            },
                            colors = textFieldColors
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Price Range Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Min Price
                            OutlinedTextField(
                                value = minPrice,
                                onValueChange = { minPrice = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Min Price", color = Color.Gray) },
                                placeholder = { Text("0", color = Color.Gray) },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachMoney, contentDescription = "Min Price", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                },
                                colors = textFieldColors
                            )

                            // Max Price
                            OutlinedTextField(
                                value = maxPrice,
                                onValueChange = { maxPrice = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Max Price", color = Color.Gray) },
                                placeholder = { Text("500", color = Color.Gray) },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachMoney, contentDescription = "Max Price", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                },
                                colors = textFieldColors
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Guests Filter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Guests:",
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    if (selectedGuests > 1) {
                                        selectedGuests--
                                        applyFilters(
                                            allHotels = allHotels,
                                            searchQuery = searchQuery,
                                            location = selectedLocation,
                                            guests = selectedGuests,
                                            minPrice = minPrice,
                                            maxPrice = maxPrice,
                                            onResult = { filteredHotels = it }
                                        )
                                    }
                                },
                                enabled = selectedGuests > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF00B4D8))
                            }

                            Text(
                                text = "$selectedGuests",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    selectedGuests++
                                    applyFilters(
                                        allHotels = allHotels,
                                        searchQuery = searchQuery,
                                        location = selectedLocation,
                                        guests = selectedGuests,
                                        minPrice = minPrice,
                                        maxPrice = maxPrice,
                                        onResult = { filteredHotels = it }
                                    )
                                },
                                enabled = selectedGuests < 10
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF00B4D8))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Apply Filters Button
                        Button(
                            onClick = {
                                applyFilters(
                                    allHotels = allHotels,
                                    searchQuery = searchQuery,
                                    location = selectedLocation,
                                    guests = selectedGuests,
                                    minPrice = minPrice,
                                    maxPrice = maxPrice,
                                    onResult = { filteredHotels = it }
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

                        // Clear Filters Button
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                selectedLocation = ""
                                selectedGuests = 1
                                minPrice = ""
                                maxPrice = ""
                                filteredHotels = allHotels
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Filters", color = Color(0xFF00B4D8))
                        }
                    }
                }
            }

            // Hotels List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00B4D8))
                }
            } else {
                if (filteredHotels.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Hotel,
                                contentDescription = "No Hotels",
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = if (showFilters || searchQuery.isNotEmpty()) {
                                    "No hotels match your search criteria"
                                } else {
                                    "No hotels available"
                                },
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            if (showFilters || searchQuery.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedLocation = ""
                                        selectedGuests = 1
                                        minPrice = ""
                                        maxPrice = ""
                                        filteredHotels = allHotels
                                    }
                                ) {
                                    Text("Clear Filters", color = Color(0xFF00B4D8))
                                }
                            }
                        }
                    }
                } else {
                    // Results count
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2A3F)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${filteredHotels.size} hotels found",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            if (filteredHotels.size != allHotels.size) {
                                TextButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedLocation = ""
                                        selectedGuests = 1
                                        minPrice = ""
                                        maxPrice = ""
                                        filteredHotels = allHotels
                                    }
                                ) {
                                    Text("Clear", color = Color(0xFF00B4D8), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredHotels) { hotel ->
                            HotelCard(
                                hotel = hotel,
                                onClick = {
                                    selectedHotel = hotel
                                    showHotelDetailsDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Hotel Details Dialog
    if (showHotelDetailsDialog && selectedHotel != null) {
        AlertDialog(
            onDismissRequest = { showHotelDetailsDialog = false },
            title = {
                Text(
                    selectedHotel!!.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${selectedHotel!!.rating} (${selectedHotel!!.reviewCount} reviews)",
                            color = Color.White
                        )
                    }

                    // Price
                    Text(
                        "Price Range: ${selectedHotel!!.priceRange}",
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Contact
                    Text(
                        "📞 ${selectedHotel!!.contactNumber}",
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Address
                    Text(
                        "📍 ${selectedHotel!!.address}",
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Amenities
                    if (selectedHotel!!.amenities.isNotEmpty()) {
                        Text(
                            "Amenities:",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            selectedHotel!!.amenities.joinToString(", "),
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Description
                    Text(
                        selectedHotel!!.description,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onHotelSelected(selectedHotel!!)
                        showHotelDetailsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B4D8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Book Now")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showHotelDetailsDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color(0xFF00B4D8))
                }
            },
            containerColor = Color(0xFF1A2A3F),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

// Filter function
private fun applyFilters(
    allHotels: List<HotelModel>,
    searchQuery: String,
    location: String,
    guests: Int,
    minPrice: String,
    maxPrice: String,
    onResult: (List<HotelModel>) -> Unit
) {
    var filtered = allHotels

    // Apply search query filter
    if (searchQuery.isNotBlank()) {
        filtered = filtered.filter { hotel ->
            hotel.name.contains(searchQuery, ignoreCase = true) ||
                    hotel.address.contains(searchQuery, ignoreCase = true) ||
                    hotel.description.contains(searchQuery, ignoreCase = true) ||
                    hotel.amenities.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Apply location filter
    if (location.isNotBlank()) {
        filtered = filtered.filter { hotel ->
            hotel.address.contains(location, ignoreCase = true)
        }
    }

    // Apply price filter
    if (minPrice.isNotBlank()) {
        val minPriceValue = minPrice.toDoubleOrNull() ?: 0.0
        filtered = filtered.filter { hotel ->
            try {
                val priceRange = hotel.priceRange.replace("[^0-9.-]".toRegex(), "")
                val parts = priceRange.split("-")
                val hotelMinPrice = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                hotelMinPrice >= minPriceValue
            } catch (e: Exception) {
                true // If price parsing fails, include the hotel
            }
        }
    }

    if (maxPrice.isNotBlank()) {
        val maxPriceValue = maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE
        filtered = filtered.filter { hotel ->
            try {
                val priceRange = hotel.priceRange.replace("[^0-9.-]".toRegex(), "")
                val parts = priceRange.split("-")
                val hotelMinPrice = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                hotelMinPrice <= maxPriceValue
            } catch (e: Exception) {
                true // If price parsing fails, include the hotel
            }
        }
    }

    onResult(filtered)
}

@Composable
fun HotelCard(
    hotel: HotelModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2A3F)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Hotel Image
            if (hotel.imageUrls.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = hotel.imageUrls.firstOrNull() ?: ""
                    ),
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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
                    text = hotel.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
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
                        text = "%.1f".format(hotel.rating),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
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
                    text = hotel.address,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price and Reviews
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price Range",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = hotel.priceRange,
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Review count
                Text(
                    text = "${hotel.reviewCount} reviews",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amenities Preview
            if (hotel.amenities.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Amenities",
                        tint = Color(0xFF00B4D8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hotel.amenities.take(3).joinToString(" • "),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}