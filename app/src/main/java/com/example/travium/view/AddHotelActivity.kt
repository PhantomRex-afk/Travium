// AddHotelActivity.kt
package com.example.travium.view

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.HotelModel
import com.example.travium.repository.HotelRepoImpl
import com.example.travium.ui.theme.*
import com.example.travium.viewmodel.AddHotelViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class AddHotelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val hotelViewModel: AddHotelViewModel = viewModel(
                factory = AddHotelViewModelFactory(HotelRepoImpl())
            )

            Scaffold(
                containerColor = AdminDeepNavy,
                topBar = {
                    Surface(color = AdminCardNavy, shadowElevation = 8.dp) {
                        Column {
                            CenterAlignedTopAppBar(
                                title = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Add New Hotel",
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            "Travium Admin",
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AdminSoftGray
                                            )
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color.Transparent
                                ),
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            finish()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { /* Handle Help */ }) {
                                        Icon(
                                            Icons.Default.Help,
                                            contentDescription = "Help",
                                            tint = Color.White
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(AdminDeepNavy)
                ) {
                    AddHotelScreen(hotelViewModel)
                }
            }
        }
    }
}

@Composable
fun AddHotelScreen(hotelViewModel: AddHotelViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context) }

    var hotelName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var amenities by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPublishing by remember { mutableStateOf(false) }

    var hotelLocation by remember { mutableStateOf<LatLng?>(null) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var locationSearchQuery by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris != null) {
            selectedImageUris = uris
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(27.7172, 85.3240), 12f)
    }

    val searchLocation = {
        coroutineScope.launch {
            try {
                val query = if (locationSearchQuery.lowercase().contains("nepal"))
                    locationSearchQuery
                else
                    "$locationSearchQuery, Nepal"

                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(query, 1)
                }

                if (addresses != null && addresses.isNotEmpty()) {
                    val addressResult = addresses[0]
                    val latLng = LatLng(addressResult.latitude, addressResult.longitude)
                    hotelLocation = latLng
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 14f))

                    if (address.isEmpty()) {
                        address = addressResult.getAddressLine(0) ?: ""
                    }
                    if (hotelName.isEmpty()) {
                        hotelName = addressResult.featureName ?: ""
                    }
                } else {
                    Toast.makeText(context, "Location not found in Nepal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hotel Name
        OutlinedTextField(
            value = hotelName,
            onValueChange = { hotelName = it },
            label = { Text("Hotel Name *", color = AdminSoftGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal,
                unfocusedBorderColor = AdminSoftGray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Image Picker
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (selectedImageUris.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            launcher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AdminAccentTeal,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Add Hotel Photos", color = AdminSoftGray)
                        Text("(Tap to select multiple)", color = AdminSoftGray.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(selectedImageUris) { uri ->
                        Box(modifier = Modifier.size(120.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable {
                                        selectedImageUris = selectedImageUris.filter { it != uri }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    launcher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            color = Color.White.copy(alpha = 0.05f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AdminAccentTeal,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Map Section
        Column {
            Text(
                "Set Hotel Location *",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Location Search
            OutlinedTextField(
                value = locationSearchQuery,
                onValueChange = { locationSearchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search hotel location in Nepal...",
                        color = AdminSoftGray,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = AdminAccentTeal,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { searchLocation() }) {
                        Icon(
                            Icons.Default.Search,
                            "Search Location",
                            tint = AdminAccentTeal
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = AdminCardNavy,
                    unfocusedContainerColor = AdminCardNavy
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { searchLocation() }),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = mapType
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = true,
                        mapToolbarEnabled = true
                    ),
                    onMapClick = { latLng ->
                        hotelLocation = latLng
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng), 300)
                        }
                    }
                ) {
                    hotelLocation?.let { latLng ->
                        Marker(
                            state = rememberMarkerState(
                                key = "hotel_location",
                                position = latLng
                            ),
                            title = hotelName.ifEmpty { "Hotel Location" },
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                }

                // Map Type Toggle
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    IconButton(onClick = { mapType = MapType.NORMAL }) {
                        Icon(
                            Icons.Default.Map,
                            null,
                            tint = if (mapType == MapType.NORMAL) AdminAccentTeal else Color.White
                        )
                    }
                    IconButton(onClick = { mapType = MapType.HYBRID }) {
                        Icon(
                            Icons.Default.Satellite,
                            null,
                            tint = if (mapType == MapType.HYBRID) AdminAccentTeal else Color.White
                        )
                    }
                    IconButton(onClick = { mapType = MapType.TERRAIN }) {
                        Icon(
                            Icons.Default.Terrain,
                            null,
                            tint = if (mapType == MapType.TERRAIN) AdminAccentTeal else Color.White
                        )
                    }
                }

                // Location Status Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    Surface(
                        color = if (hotelLocation != null) AdminAccentTeal.copy(alpha = 0.9f)
                        else AdminAlertRed.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (hotelLocation != null) Icons.Default.CheckCircle else Icons.Default.Warning,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (hotelLocation != null) "Location Set" else "Tap map to set location",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Address
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Full Address *", color = AdminSoftGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Contact & Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact", color = AdminSoftGray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AdminAccentTeal
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = priceRange,
                onValueChange = { priceRange = it },
                label = { Text("Price Range", color = AdminSoftGray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AdminAccentTeal
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = { Text("e.g., $50-$200", color = AdminSoftGray.copy(alpha = 0.5f)) }
            )
        }

        // Amenities
        OutlinedTextField(
            value = amenities,
            onValueChange = { amenities = it },
            label = { Text("Amenities (comma separated)", color = AdminSoftGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            placeholder = { Text("e.g., WiFi, Pool, Spa, Gym", color = AdminSoftGray.copy(alpha = 0.5f)) }
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Hotel Description", color = AdminSoftGray) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AdminAccentTeal
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Publish Button
        Button(
            onClick = {
                if (hotelName.isBlank()) {
                    Toast.makeText(context, "Please enter hotel name", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (hotelLocation == null) {
                    Toast.makeText(context, "Please set hotel location on map", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (address.isBlank()) {
                    Toast.makeText(context, "Please enter hotel address", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isPublishing = true

                // Create hotel model
                val hotel = HotelModel(
                    hotelId = "",
                    name = hotelName,
                    description = description,
                    address = address,
                    contactNumber = contactNumber,
                    priceRange = priceRange,
                    amenities = amenities.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    latitude = hotelLocation?.latitude,
                    longitude = hotelLocation?.longitude,
                    imageUrls = emptyList(),
                    rating = 0.0,
                    reviewCount = 0,
                    createdAt = System.currentTimeMillis(),
                    ownerId = "admin"
                )

                // Call viewModel to add hotel
                hotelViewModel.addHotel(
                    context = context,
                    hotel = hotel,
                    imageUris = selectedImageUris
                ) { success, message ->
                    isPublishing = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    if (success) {
                        // Clear form
                        hotelName = ""
                        description = ""
                        address = ""
                        contactNumber = ""
                        priceRange = ""
                        amenities = ""
                        selectedImageUris = emptyList()
                        hotelLocation = null
                        locationSearchQuery = ""

                        // Return to admin dashboard after a short delay
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(1500)
                            (context as? android.app.Activity)?.finish()
                        }
                    }
                }
            },
            enabled = !isPublishing,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AdminAccentTeal,
                disabledContainerColor = AdminAccentTeal.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isPublishing) {
                CircularProgressIndicator(
                    color = AdminDeepNavy,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Hotel,
                        contentDescription = null,
                        tint = AdminDeepNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Publish Hotel",
                        color = AdminDeepNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Cancel Button
        TextButton(
            onClick = {
                (context as? android.app.Activity)?.finish()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Cancel",
                color = AdminSoftGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

class AddHotelViewModelFactory(private val hotelRepo: HotelRepoImpl) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddHotelViewModel::class.java)) {
            return AddHotelViewModel(hotelRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}