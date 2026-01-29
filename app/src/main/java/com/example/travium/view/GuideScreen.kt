package com.example.travium.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.model.GuideModel
import com.example.travium.repository.GuideRepoImpl
import com.example.travium.viewmodel.GuideViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

@Composable
fun GuideScreenBody() {
    val guideViewModel = remember { GuideViewModel(GuideRepoImpl()) }
    val allGuides by guideViewModel.allGuides.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        guideViewModel.getAllGuides()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TravelDeepNavy)
    ) {
        if (allGuides.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Explore local travel guides.", color = TravelSoftGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allGuides) { guide ->
                    UserGuideCard(guide = guide)
                }
            }
        }
    }
}

@Composable
fun UserGuideCard(guide: GuideModel) {
    var isExpanded by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(guide.latitude, guide.longitude), 15f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardNavy)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TravelAccentTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = guide.placeName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TravelAccentTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Teeny Tiny Image Row
            if (guide.imageUrls.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(guide.imageUrls) { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = null,
                            modifier = Modifier
                                .size(140.dp, 90.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("Interactive Guide Map", color = TravelAccentTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
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
                                mapStyleOptions = MapStyleOptions(TRAVEL_MAP_STYLE)
                            ),
                            uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = true, mapToolbarEnabled = true)
                        ) {
                            // Main Destination Pin (Azure)
                            Marker(
                                state = rememberMarkerState(key = "user_dest_${guide.guideId}_${guide.latitude}", position = LatLng(guide.latitude, guide.longitude)),
                                title = guide.placeName,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                            )
                            
                            // Officially Recommended Hotel Pins (Orange)
                            guide.hotels.forEach { hotel ->
                                Marker(
                                    state = rememberMarkerState(key = "user_hotel_${hotel.name}_${hotel.latitude}_${hotel.longitude}", position = LatLng(hotel.latitude, hotel.longitude)),
                                    title = hotel.name,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                                )
                            }
                        }
                    }

                    if (guide.accommodations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Description & Accommodations", color = TravelAccentTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = guide.accommodations,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
