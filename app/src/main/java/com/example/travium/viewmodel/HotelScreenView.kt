// HotelScreenView.kt
package com.example.travium.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travium.model.HotelModel
import com.example.travium.repository.HotelRepoImpl
import com.example.travium.ui.theme.*
import com.example.travium.viewmodel.HotelViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun HotelScreenView(
    hotelId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val hotelViewModel: HotelViewModel = viewModel(
        factory = HotelViewModelFactory(HotelRepoImpl())
    )
    val allHotels by hotelViewModel.allHotels.collectAsState()
    val selectedHotel by hotelViewModel.selectedHotel.collectAsState()

    // Get hotel from either allHotels list or selectedHotel
    var hotel by remember { mutableStateOf<HotelModel?>(null) }

    LaunchedEffect(hotelId, allHotels, selectedHotel) {
        hotel = allHotels.find { it.hotelId == hotelId } ?: selectedHotel
    }

    LaunchedEffect(key1 = hotelId) {
        if (hotel == null) {
            hotelViewModel.getHotelById(hotelId)
        }
    }

    if (hotel == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminDeepNavy),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AdminAccentTeal)
        }
        return
    }

    HotelScreenContent(
        hotel = hotel!!,
        onBack = onBack,
        onEdit = { onEdit(hotelId) }
    )
}

@Composable
private fun HotelScreenContent(
    hotel: HotelModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(27.7172, 85.3240),
            12f
        )
    }

    LaunchedEffect(hotel.latitude, hotel.longitude) {
        if (hotel.latitude != null && hotel.longitude != null) {
            val latLng = LatLng(hotel.latitude, hotel.longitude)
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = AdminCardNavy,
                shadowElevation = 8.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = hotel.name,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Hotel Details",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AdminSoftGray
                                )
                            )
                        }

                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Hotel",
                                tint = Color.White
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                    )
                }
            }
        },
        containerColor = AdminDeepNavy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hotel Images
            if (hotel.imageUrls.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(AdminCardNavy)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(hotel.imageUrls.firstOrNull() ?: ""),
                        contentDescription = "Hotel Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Image Counter
                    if (hotel.imageUrls.size > 1) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Text(
                                "+${hotel.imageUrls.size - 1} more",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Hotel Details
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hotel Name and Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            hotel.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            hotel.address,
                            color = AdminSoftGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Rating
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "%.1f".format(hotel.rating),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Text(
                            "${hotel.reviewCount} reviews",
                            color = AdminSoftGray,
                            fontSize = 12.sp
                        )
                    }
                }

                // Contact and Price
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AdminCardNavy
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Contact",
                                tint = AdminAccentTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Contact",
                                color = AdminSoftGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                hotel.contactNumber,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = "Price",
                                tint = AdminAccentTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Price Range",
                                color = AdminSoftGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                hotel.priceRange,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Location Map
                if (hotel.latitude != null && hotel.longitude != null) {
                    Column {
                        Text(
                            "Location",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    mapType = MapType.NORMAL
                                ),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    scrollGesturesEnabled = true,
                                    mapToolbarEnabled = false
                                )
                            ) {
                                Marker(
                                    state = rememberMarkerState(
                                        key = "hotel_location",
                                        position = LatLng(hotel.latitude!!, hotel.longitude!!)
                                    ),
                                    title = hotel.name,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                )
                            }
                        }
                        Text(
                            hotel.address,
                            color = AdminSoftGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Description
                if (hotel.description.isNotBlank()) {
                    Column {
                        Text(
                            "Description",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            hotel.description,
                            color = AdminSoftGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Amenities
                if (hotel.amenities.isNotEmpty()) {
                    Column {
                        Text(
                            "Amenities",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            hotel.amenities.forEach { amenity ->
                                Surface(
                                    color = AdminAccentTeal.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        amenity,
                                        color = AdminAccentTeal,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}