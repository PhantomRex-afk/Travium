// HotelScreen.kt
package com.example.travium.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travium.model.HotelModel
import com.example.travium.repository.HotelRepoImpl
import com.example.travium.ui.theme.*
import com.example.travium.viewmodel.HotelViewModel

@Composable
fun HotelScreen(
    onAddHotel: () -> Unit,
    onViewHotel: (String) -> Unit,
    onEditHotel: (String) -> Unit
) {
    val context = LocalContext.current
    val hotelViewModel: HotelViewModel = viewModel(
        factory = HotelViewModelFactory(HotelRepoImpl())
    )
    val allHotels by hotelViewModel.allHotels.collectAsState()
    val isLoading by hotelViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        hotelViewModel.getAllHotels()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDeepNavy)
    ) {
        // Header
        Surface(
            color = AdminCardNavy,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Hotel Management",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    // Add Hotel Button
                    Button(
                        onClick = onAddHotel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AdminAccentTeal
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Hotel",
                            tint = AdminDeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Hotel", color = AdminDeepNavy, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    "Manage all registered hotels in the platform",
                    color = AdminSoftGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminAccentTeal)
            }
        } else if (allHotels.isEmpty()) {
            EmptyHotelState(onAddHotel = onAddHotel)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allHotels) { hotel ->
                    HotelCard(
                        hotel = hotel,
                        onView = { onViewHotel(hotel.hotelId) },
                        onEdit = { onEditHotel(hotel.hotelId) },
                        onDelete = {
                            hotelViewModel.deleteHotel(hotel.hotelId) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HotelCard(
    hotel: HotelModel,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AdminCardNavy,
            title = {
                Text("Delete Hotel?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete ${hotel.name}? This action cannot be undone.",
                    color = AdminSoftGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminAlertRed
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel", color = AdminSoftGray)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onView),
        colors = CardDefaults.cardColors(
            containerColor = AdminCardNavy
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Hotel Name and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        hotel.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = AdminAccentTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            hotel.address.takeIf { it.isNotBlank() } ?: "No address provided",
                            color = AdminSoftGray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                // Action Buttons
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                AdminAccentTeal.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AdminAccentTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                AdminAlertRed.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AdminAlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Hotel Images
            if (hotel.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hotel.imageUrls.take(3)) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Hotel Image",
                            modifier = Modifier
                                .size(120.dp, 80.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Hotel Details
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Price Range
                Column {
                    Text(
                        "Price Range",
                        color = AdminSoftGray,
                        fontSize = 12.sp
                    )
                    Text(
                        hotel.priceRange.takeIf { it.isNotBlank() } ?: "Not specified",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Contact
                Column {
                    Text(
                        "Contact",
                        color = AdminSoftGray,
                        fontSize = 12.sp
                    )
                    Text(
                        hotel.contactNumber.takeIf { it.isNotBlank() } ?: "Not provided",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Amenities
            if (hotel.amenities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Amenities",
                    color = AdminSoftGray,
                    fontSize = 12.sp
                )
                Text(
                    hotel.amenities.joinToString(", "),
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Rating
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Rating: ${"%.1f".format(hotel.rating)} (${hotel.reviewCount} reviews)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun EmptyHotelState(onAddHotel: () -> Unit) {
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
                tint = AdminSoftGray,
                modifier = Modifier.size(80.dp)
            )

            Text(
                "No Hotels Found",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Text(
                "Add your first hotel to get started",
                color = AdminSoftGray,
                fontSize = 14.sp
            )

            Button(
                onClick = onAddHotel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminAccentTeal
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Hotel",
                    tint = AdminDeepNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Hotel", color = AdminDeepNavy, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ViewModel Factory
class HotelViewModelFactory(private val hotelRepo: HotelRepoImpl) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HotelViewModel::class.java)) {
            return HotelViewModel(hotelRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}