package com.example.travium.view

import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.travium.R
import com.example.travium.model.GuideModel
import com.example.travium.model.HotelLocation
import com.example.travium.model.MakePostModel
import com.example.travium.model.UserModel
import com.example.travium.repository.GuideRepoImpl
import com.example.travium.repository.MakePostRepoImpl
import com.example.travium.repository.UserRepoImpl
import com.example.travium.viewmodel.GuideViewModel
import com.example.travium.viewmodel.MakePostViewModel
import com.example.travium.viewmodel.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Enhanced Admin-themed colors
val AdminDeepNavy = Color(0xFF0F172A)
val AdminCardNavy = Color(0xFF1E293B)
val AdminAccentTeal = Color(0xFF2DD4BF)
val AdminSoftGray = Color(0xFF94A3B8)
val AdminAlertRed = Color(0xFFEF4444)

class AdminDashboardActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent {
            val postViewModel = remember { MakePostViewModel(MakePostRepoImpl()) }
            val userViewModel = remember { UserViewModel(UserRepoImpl()) }
            val guideViewModel = remember { GuideViewModel(GuideRepoImpl()) }
            var selectedIndex by remember { mutableIntStateOf(0) }
            var selectedGuideForEdit by remember { mutableStateOf<GuideModel?>(null) }
            
            Scaffold(
                containerColor = AdminDeepNavy,
                topBar = {
                    Surface(color = AdminCardNavy, shadowElevation = 8.dp) {
                        Column {
                            CenterAlignedTopAppBar(
                                title = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Travium", style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                                        Text("Admin Dashboard", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AdminSoftGray))
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                                navigationIcon = {
                                    if (selectedGuideForEdit != null) {
                                        IconButton(onClick = { selectedGuideForEdit = null }) {
                                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                                        }
                                    }
                                }
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                        }
                    }
                },
                bottomBar = {
                    if (selectedGuideForEdit == null) {
                        Surface(color = AdminCardNavy, shadowElevation = 16.dp) {
                            Column {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                                NavigationBar(containerColor = AdminCardNavy, tonalElevation = 8.dp) {
                                    val items = listOf(
                                        Triple("Home", R.drawable.outline_home_24, "Home"),
                                        Triple("Add", R.drawable.addbox, "Add Guide"),
                                        Triple("Manage", R.drawable.outline_map_pin_review_24, "Manage Guides"),
                                        Triple("Users", R.drawable.profile, "Users List")
                                    )
                                    items.forEachIndexed { index, item ->
                                        NavigationBarItem(
                                            icon = { Icon(painterResource(item.second), item.third) },
                                            label = { Text(item.first) },
                                            selected = selectedIndex == index,
                                            onClick = { selectedIndex = index },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = AdminAccentTeal, indicatorColor = AdminAccentTeal.copy(alpha = 0.1f), selectedTextColor = AdminAccentTeal
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when {
                        selectedGuideForEdit != null -> EditGuideScreen(selectedGuideForEdit!!, guideViewModel) { selectedGuideForEdit = null }
                        selectedIndex == 0 -> AdminHomeFeed(postViewModel, userViewModel)
                        selectedIndex == 1 -> AddGuideScreen(guideViewModel)
                        selectedIndex == 2 -> AdminGuideList(guideViewModel) { selectedGuideForEdit = it }
                        selectedIndex == 3 -> AdminUsersList(userViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun EditGuideScreen(guide: GuideModel, guideViewModel: GuideViewModel, onComplete: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context) }
    
    var placeName by remember { mutableStateOf(guide.placeName) }
    var accommodations by remember { mutableStateOf(guide.accommodations) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImageUrls by remember { mutableStateOf(guide.imageUrls) }
    var isPublishing by remember { mutableStateOf(false) }
    
    var destinationLocation by remember { mutableStateOf<LatLng?>(LatLng(guide.latitude, guide.longitude)) }
    val hotelLocations = remember { mutableStateListOf<HotelLocation>().apply { addAll(guide.hotels) } }
    var showHotelNameDialog by remember { mutableStateOf<LatLng?>(null) }
    var tempHotelName by remember { mutableStateOf("") }
    var mapType by remember { mutableStateOf(MapType.HYBRID) }
    
    var hotelSearchQuery by remember { mutableStateOf("") }
    val foundHotels = remember { mutableStateListOf<HotelLocation>() }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia()) { uris -> selectedImageUris = selectedImageUris + uris }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(guide.latitude, guide.longitude), 15f) }

    val searchExistingHotels = {
        coroutineScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName("${hotelSearchQuery} near ${placeName}", 10)
                }
                foundHotels.clear()
                results?.forEach { addr -> foundHotels.add(HotelLocation(addr.featureName ?: "Hotel", addr.latitude, addr.longitude)) }
            } catch (e: Exception) { Toast.makeText(context, "Search failed", Toast.LENGTH_SHORT).show() }
        }
    }

    if (showHotelNameDialog != null) {
        AlertDialog(onDismissRequest = { showHotelNameDialog = null }, containerColor = AdminCardNavy, title = { Text("Marker Name", color = Color.White) }, text = { OutlinedTextField(value = tempHotelName, onValueChange = { tempHotelName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdminAccentTeal)) }, confirmButton = { TextButton(onClick = { if (tempHotelName.isNotBlank()) { hotelLocations.add(HotelLocation(tempHotelName, showHotelNameDialog!!.latitude, showHotelNameDialog!!.longitude)); tempHotelName = ""; showHotelNameDialog = null } }) { Text("Save Pin", color = AdminAccentTeal) } })
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Update Journey Guide", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        OutlinedTextField(value = placeName, onValueChange = { placeName = it }, label = { Text("Destination Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdminAccentTeal))

        // Existing Images
        if (existingImageUrls.isNotEmpty()) {
            Column {
                Text("Existing Photos", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(existingImageUrls) { url ->
                        Box(modifier = Modifier.size(100.dp)) {
                            Image(rememberAsyncImagePainter(url), null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                            IconButton(onClick = { existingImageUrls = existingImageUrls.filter { it != url } }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
            }
        }

        // Add New Images
        Button(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = AdminCardNavy), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.AddPhotoAlternate, null, tint = AdminAccentTeal); Spacer(Modifier.width(8.dp)); Text("Add More Photos") }

        // Map Section
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(20.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))) {
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = MapProperties(mapStyleOptions = MapStyleOptions(TRAVEL_MAP_STYLE), mapType = mapType), onMapLongClick = { destinationLocation = it }) {
                destinationLocation?.let { Marker(state = rememberMarkerState(key = "edit_dest", position = it), title = "Main Dest", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) }
                hotelLocations.forEach { hotel -> Marker(state = rememberMarkerState(key = "edit_hotel_${hotel.name}", position = LatLng(hotel.latitude, hotel.longitude)), title = hotel.name, icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)) }
            }
        }

        OutlinedTextField(value = accommodations, onValueChange = { accommodations = it }, label = { Text("Accommodation Details") }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdminAccentTeal))

        Button(
            onClick = {
                isPublishing = true
                guideViewModel.updateGuide(context, guide.guideId, placeName, selectedImageUris, existingImageUrls, accommodations, destinationLocation?.latitude ?: 0.0, destinationLocation?.longitude ?: 0.0, hotelLocations.toList()) { s, m ->
                    isPublishing = false
                    Toast.makeText(context, m, Toast.LENGTH_SHORT).show()
                    if (s) onComplete()
                }
            },
            enabled = !isPublishing,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminAccentTeal)
        ) {
            if (isPublishing) CircularProgressIndicator(color = AdminDeepNavy, modifier = Modifier.size(24.dp))
            else Text("Apply Updates", color = AdminDeepNavy, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminGuideList(guideViewModel: GuideViewModel, onEdit: (GuideModel) -> Unit) {
    val allGuides by guideViewModel.allGuides.observeAsState(initial = emptyList())
    val context = LocalContext.current
    LaunchedEffect(Unit) { guideViewModel.getAllGuides() }
    Box(modifier = Modifier.fillMaxSize()) {
        if (allGuides.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No guides available", color = AdminSoftGray) } }
        else { LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(allGuides) { guide -> AdminGuideCard(guide, onEdit) { guideViewModel.deleteGuide(guide.guideId) { s, m -> Toast.makeText(context, m, Toast.LENGTH_SHORT).show() } } } } }
    }
}

@Composable
fun AdminGuideCard(guide: GuideModel, onEdit: (GuideModel) -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(guide.latitude, guide.longitude), 15f) }

    if (showDeleteDialog) { AlertDialog(onDismissRequest = { showDeleteDialog = false }, containerColor = AdminCardNavy, title = { Text("Delete Guide?", color = Color.White) }, confirmButton = { Button(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AdminAlertRed)) { Text("Delete") } }) }
    
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)).border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AdminCardNavy)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = AdminAccentTeal, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(text = guide.placeName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) }
                Row {
                    IconButton(onClick = { onEdit(guide) }) { Icon(Icons.Default.Edit, null, tint = AdminAccentTeal) }
                    IconButton(onClick = { isExpanded = !isExpanded }) { Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = AdminAccentTeal) }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.background(AdminAlertRed.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Default.Delete, null, tint = AdminAlertRed, modifier = Modifier.size(20.dp)) }
                }
            }
            if (guide.imageUrls.isNotEmpty()) { LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(guide.imageUrls) { url -> Image(rememberAsyncImagePainter(url), null, modifier = Modifier.size(140.dp, 90.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop) } } }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))) {
                        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = MapProperties(mapStyleOptions = MapStyleOptions(TRAVEL_MAP_STYLE)), uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = true, mapToolbarEnabled = true)) {
                            Marker(state = rememberMarkerState(key = "manage_dest_${guide.guideId}", position = LatLng(guide.latitude, guide.longitude)), title = guide.placeName, icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            guide.hotels.forEach { hotel -> Marker(state = rememberMarkerState(key = "manage_hotel_${hotel.name}", position = LatLng(hotel.latitude, hotel.longitude)), title = hotel.name, icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)) }
                        }
                    }
                    if (guide.accommodations.isNotEmpty()) { Spacer(modifier = Modifier.height(16.dp)); Text("Details", color = AdminAccentTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(text = guide.accommodations, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, lineHeight = 20.sp) }
                }
            }
        }
    }
}

@Composable
fun AddGuideScreen(guideViewModel: GuideViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context) }
    
    var placeName by remember { mutableStateOf("") }
    var accommodations by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPublishing by remember { mutableStateOf(false) }
    
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    val hotelLocations = remember { mutableStateListOf<HotelLocation>() }
    var showHotelNameDialog by remember { mutableStateOf<LatLng?>(null) }
    var tempHotelName by remember { mutableStateOf("") }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    
    var placeSearchQuery by remember { mutableStateOf("") }
    var hotelSearchQuery by remember { mutableStateOf("") }
    val foundHotels = remember { mutableStateListOf<HotelLocation>() }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia()) { uris -> selectedImageUris = uris }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(28.3949, 84.1240), 7f) }

    val mapProperties = remember(mapType) { MapProperties(mapStyleOptions = MapStyleOptions(TRAVEL_MAP_STYLE), mapType = mapType) }
    val mapUiSettings = remember { MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = true, mapToolbarEnabled = true) }

    val searchPlace = {
        coroutineScope.launch {
            try {
                val query = if (placeSearchQuery.lowercase().contains("nepal")) placeSearchQuery else "$placeSearchQuery, Nepal"
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(query, 1)
                }
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    destinationLocation = latLng
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                    if (placeName.isBlank()) placeName = address.featureName ?: ""
                } else {
                    Toast.makeText(context, "Place not found in Nepal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Search failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val searchExistingHotels = {
        coroutineScope.launch {
            try {
                if (destinationLocation == null) {
                    Toast.makeText(context, "Pin a destination first", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val query = if (hotelSearchQuery.isNotBlank()) hotelSearchQuery else "Hotels"
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName("$query near $placeName", 10)
                }
                foundHotels.clear()
                results?.forEach { addr ->
                    foundHotels.add(HotelLocation(addr.featureName ?: "Hotel", addr.latitude, addr.longitude))
                }
                if (foundHotels.isEmpty()) Toast.makeText(context, "No hotels found nearby", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Hotel search failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showHotelNameDialog != null) {
        AlertDialog(
            onDismissRequest = { showHotelNameDialog = null },
            containerColor = AdminCardNavy,
            title = { Text("Set Recommendation Name", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("What should the users see as the name for this pin?", color = AdminSoftGray, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempHotelName,
                        onValueChange = { tempHotelName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdminAccentTeal),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempHotelName.isNotBlank()) {
                        hotelLocations.add(HotelLocation(tempHotelName, showHotelNameDialog!!.latitude, showHotelNameDialog!!.longitude))
                        tempHotelName = ""
                        showHotelNameDialog = null
                    }
                }) { Text("Confirm Pin", color = AdminAccentTeal, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showHotelNameDialog = null }) { Text("Cancel", color = Color.White) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Create New Journey Guide", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        OutlinedTextField(
            value = placeName,
            onValueChange = { placeName = it },
            label = { Text("Destination Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdminAccentTeal),
            shape = RoundedCornerShape(12.dp)
        )

        // Integrated Image Picker
        Card(modifier = Modifier.fillMaxWidth().height(160.dp), colors = CardDefaults.cardColors(containerColor = AdminCardNavy), shape = RoundedCornerShape(16.dp)) {
            if (selectedImageUris.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = AdminAccentTeal, modifier = Modifier.size(48.dp))
                        Text("Add Multiple Pictures", color = AdminSoftGray)
                    }
                }
            } else {
                LazyRow(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    items(selectedImageUris) { uri ->
                        Box(modifier = Modifier.size(110.dp)) {
                            Image(rememberAsyncImagePainter(uri), null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)).clickable { selectedImageUris = selectedImageUris.filter { it != uri } }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    item {
                        Surface(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(12.dp)).clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, color = Color.White.copy(alpha = 0.05f)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = AdminAccentTeal, modifier = Modifier.size(32.dp)) }
                        }
                    }
                }
            }
        }

        // Map Section with Search
        Column {
            Text("Set Location & Recommendations", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(450.dp).clip(RoundedCornerShape(20.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))) {
                Column {
                    // Place Search Bar
                    OutlinedTextField(
                        value = placeSearchQuery,
                        onValueChange = { placeSearchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        placeholder = { Text("Find destination in Nepal...", color = AdminSoftGray, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = AdminAccentTeal, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { 
                            IconButton(onClick = { searchPlace() }) { 
                                Icon(Icons.Default.Search, "Search Place", tint = AdminAccentTeal) 
                            } 
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = AdminCardNavy, unfocusedContainerColor = AdminCardNavy),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { searchPlace() }),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Hotel Search Bar (only shows if destination is pinned)
                    if (destinationLocation != null) {
                        OutlinedTextField(
                            value = hotelSearchQuery,
                            onValueChange = { hotelSearchQuery = it },
                            placeholder = { Text("Find real hotels nearby...", color = AdminSoftGray, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = AdminAccentTeal, modifier = Modifier.size(18.dp)) },
                            trailingIcon = { 
                                IconButton(onClick = { searchExistingHotels() }) { 
                                    Icon(Icons.Default.Add, "Search Hotels", tint = AdminAccentTeal) 
                                } 
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = AdminCardNavy, unfocusedContainerColor = AdminCardNavy),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { searchExistingHotels() }),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = mapProperties,
                            uiSettings = mapUiSettings,
                            onMapLongClick = { latLng -> 
                                if (destinationLocation == null) destinationLocation = latLng
                                else { showHotelNameDialog = latLng; tempHotelName = "" }
                            }
                        ) {
                            destinationLocation?.let { LatLngPos ->
                                Marker(
                                    state = rememberMarkerState(key = "admin_add_dest", position = LatLngPos), 
                                    title = "Destination: $placeName", 
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                ) 
                            }
                            
                            // Found Hotels Pins (Green markers from search results)
                            foundHotels.forEach { hotel ->
                                Marker(
                                    state = rememberMarkerState(key = "found_${hotel.latitude}_${hotel.longitude}", position = LatLng(hotel.latitude, hotel.longitude)),
                                    title = hotel.name,
                                    snippet = "Tap to Mark & Name",
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                                    onClick = { 
                                        if (!hotelLocations.any { it.latitude == hotel.latitude }) {
                                            showHotelNameDialog = LatLng(hotel.latitude, hotel.longitude)
                                            tempHotelName = hotel.name
                                        }
                                        true
                                    }
                                )
                            }

                            // Officially Added Hotels Pins (Orange)
                            hotelLocations.forEach { hotelLoc ->
                                Marker(
                                    state = rememberMarkerState(key = "added_${hotelLoc.latitude}_${hotelLoc.longitude}", position = LatLng(hotelLoc.latitude, hotelLoc.longitude)), 
                                    title = hotelLoc.name, 
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                                ) 
                            }
                        }
                        
                        // Map Type Toggles
                        Column(modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))) {
                            IconButton(onClick = { mapType = MapType.NORMAL }) { Icon(Icons.Default.Map, null, tint = if (mapType == MapType.NORMAL) AdminAccentTeal else Color.White) }
                            IconButton(onClick = { mapType = MapType.HYBRID }) { Icon(Icons.Default.Satellite, null, tint = if (mapType == MapType.HYBRID) AdminAccentTeal else Color.White) }
                            IconButton(onClick = { mapType = MapType.TERRAIN }) { Icon(Icons.Default.Terrain, null, tint = if (mapType == MapType.TERRAIN) AdminAccentTeal else Color.White) }
                        }
                    }
                }
            }
            
            // Pinned Hotels Chip List
            if (hotelLocations.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(hotelLocations) { hotel ->
                        AssistChip(
                            onClick = { },
                            label = { Text(hotel.name, color = Color.White) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp).clickable { hotelLocations.remove(hotel) }, tint = Color.White) },
                            border = BorderStroke(1.dp, AdminAccentTeal.copy(alpha = 0.3f)),
                            colors = AssistChipDefaults.assistChipColors(containerColor = AdminCardNavy)
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = accommodations,
            onValueChange = { accommodations = it },
            label = { Text("Accommodation Text Details") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdminAccentTeal),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (destinationLocation == null) { Toast.makeText(context, "Set location on map", Toast.LENGTH_SHORT).show(); return@Button }
                isPublishing = true
                guideViewModel.addGuide(
                    context = context, 
                    placeName = placeName, 
                    imageUris = selectedImageUris, 
                    accommodations = accommodations,
                    latitude = destinationLocation!!.latitude,
                    longitude = destinationLocation!!.longitude,
                    hotels = hotelLocations.toList()
                ) { success, message ->
                    isPublishing = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) { 
                        placeName = ""; accommodations = ""; selectedImageUris = emptyList()
                        destinationLocation = null; hotelLocations.clear(); foundHotels.clear(); placeSearchQuery = ""; hotelSearchQuery = ""
                    }
                }
            },
            enabled = !isPublishing,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminAccentTeal, disabledContainerColor = AdminAccentTeal.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isPublishing) CircularProgressIndicator(color = AdminDeepNavy, modifier = Modifier.size(24.dp))
            else Text("Publish Guide", color = AdminDeepNavy, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AdminHomeFeed(postViewModel: MakePostViewModel, userViewModel: UserViewModel) {
    val allPosts by postViewModel.allPosts.observeAsState(initial = emptyList())
    LaunchedEffect(Unit) { postViewModel.getAllPosts() }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(allPosts) { post -> AdminPostCard(post, postViewModel, userViewModel) }
    }
}


@Composable
fun AdminUsersList(userViewModel: UserViewModel) {
    val allUsers by userViewModel.allUsers.observeAsState(initial = emptyList())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { userViewModel.getAllUsers() }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex, containerColor = AdminDeepNavy, contentColor = AdminAccentTeal, indicator = { tp -> SecondaryIndicator(Modifier.tabIndicatorOffset(tp[selectedTabIndex]), color = AdminAccentTeal) }) {
            listOf("ACTIVE", "BANNED").forEachIndexed { i, t -> Tab(selected = selectedTabIndex == i, onClick = { selectedTabIndex = i }, text = { Text(t, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black)) }, unselectedContentColor = AdminSoftGray) }
        }
        val filteredUsers = if (selectedTabIndex == 0) allUsers else emptyList()
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredUsers) { user -> UserCard(user, selectedTabIndex == 1) }
        }
    }
}

@Composable
fun UserCard(user: UserModel, isBanned: Boolean) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = AdminCardNavy)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(AdminAccentTeal, Color(0xFF3B82F6)))), contentAlignment = Alignment.Center) { Text(text = user.fullName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp) }
                Spacer(modifier = Modifier.width(16.dp)); Column { Text(user.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Email, null, tint = AdminAccentTeal, modifier = Modifier.size(12.dp)); Text(user.email, color = AdminSoftGray, fontSize = 13.sp) } }
            }
            IconButton(onClick = { Toast.makeText(context, "Action triggered", Toast.LENGTH_SHORT).show() }, modifier = Modifier.background(if (isBanned) AdminAccentTeal.copy(alpha = 0.1f) else AdminAlertRed.copy(alpha = 0.1f), CircleShape)) { Icon(if (isBanned) Icons.Default.CheckCircle else Icons.Default.Block, null, tint = if (isBanned) AdminAccentTeal else AdminAlertRed, modifier = Modifier.size(22.dp)) }
        }
    }
}

@Composable
fun AdminPostCard(post: MakePostModel, postViewModel: MakePostViewModel, userViewModel: UserViewModel) {
    val context = LocalContext.current
    var author by remember { mutableStateOf<UserModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteReason by remember { mutableStateOf("") }
    LaunchedEffect(post.userId) { userViewModel.getUserById(post.userId) { fetchedUser -> author = fetchedUser } }
    if (showDeleteDialog) { AlertDialog(onDismissRequest = { showDeleteDialog = false }, containerColor = AdminCardNavy, shape = RoundedCornerShape(24.dp), title = { Text("Delete User Post", color = Color.White, fontWeight = FontWeight.Bold) }, text = { Column { OutlinedTextField(value = deleteReason, onValueChange = { deleteReason = it }, label = { Text("Violation Reason") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdminAccentTeal, unfocusedBorderColor = Color.White.copy(alpha = 0.1f), focusedContainerColor = AdminDeepNavy, unfocusedContainerColor = AdminDeepNavy), shape = RoundedCornerShape(12.dp)) } }, confirmButton = { Button(onClick = { if (deleteReason.isNotBlank()) { postViewModel.deletePost(post.postId, post.userId, deleteReason) { s, m -> if (s) showDeleteDialog = false } } }, colors = ButtonDefaults.buttonColors(containerColor = AdminAlertRed)) { Text("Remove Post", fontWeight = FontWeight.Bold) } }) }
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AdminCardNavy)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(AdminAccentTeal, Color(0xFF3B82F6)))), contentAlignment = Alignment.Center) { Text(text = (author?.fullName?.take(1) ?: "T").uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp) }; Spacer(modifier = Modifier.width(12.dp)); Text(text = author?.fullName ?: "Traveler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.background(AdminAlertRed.copy(alpha = 0.1f), CircleShape) ) { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = AdminAlertRed, modifier = Modifier.size(20.dp)) }
            }
            if (post.caption.isNotEmpty()) Text(post.caption, color = Color.White, fontSize = 15.sp, lineHeight = 20.sp)
            if (post.location.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = AdminAccentTeal, modifier = Modifier.size(14.dp)); Text(text = post.location, color = AdminSoftGray, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            if (post.imageUrl.isNotEmpty()) Image(rememberAsyncImagePainter(post.imageUrl), null, modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
fun AdminPlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(AdminDeepNavy), contentAlignment = Alignment.Center) { Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() { AdminPlaceholderScreen(title = "Welcome Admin") }
