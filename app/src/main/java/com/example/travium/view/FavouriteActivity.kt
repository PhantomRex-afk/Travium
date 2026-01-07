package com.example.travium.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travium.model.FavouritePlace
import com.example.travium.view.ui.theme.TraviumTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FavouriteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraviumTheme {
                FavouriteScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var favouritePlaces by remember { mutableStateOf<List<FavouritePlace>>(emptyList()) }

    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val database = Firebase.database
            val myRef = database.getReference("users").child(user.uid).child("favourites")
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val places = mutableListOf<FavouritePlace>()
                    for (placeSnapshot in snapshot.children) {
                        val place = placeSnapshot.getValue(FavouritePlace::class.java)
                        if (place != null) {
                            places.add(place)
                        }
                    }
                    favouritePlaces = places
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    Scaffold(
        containerColor = Color(0xFF2C2C2C),
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Memories") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, AddFavouriteActivity::class.java))
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Favourite")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { 
            items(favouritePlaces) { place ->
                FavouritePlaceCard(
                    place = place,
                    onEdit = { /* TODO: Handle edit functionality */ },
                    onDelete = {
                        val user = Firebase.auth.currentUser
                        if (user != null && place.id != null) {
                            val database = Firebase.database
                            val myRef = database.getReference("users").child(user.uid).child("favourites").child(place.id)
                            myRef.removeValue()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FavouritePlaceCard(
    place: FavouritePlace, 
    modifier: Modifier = Modifier, 
    onEdit: () -> Unit, 
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3C3C3C))
    ) {
        ListItem(
            headlineContent = { Text(place.name ?: "", style = MaterialTheme.typography.titleMedium, color = Color.White) },
            supportingContent = { Text(place.description ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f)) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Favourite", tint = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Favourite", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavouriteScreenPreview() {
    TraviumTheme(darkTheme = true) {
        FavouriteScreen()
    }
}
