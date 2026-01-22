package com.example.travium.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.R

// Travel-themed dark colors
val AdminDeepNavy = Color(0xFF0F172A)
val AdminCardNavy = Color(0xFF1E293B)
val AdminAccentTeal = Color(0xFF2DD4BF)
val AdminSoftGray = Color(0xFF94A3B8)

class AdminDashboardActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        
        setContent {
            var selectedIndex by remember { mutableIntStateOf(0) }
            
            Scaffold(
                containerColor = AdminDeepNavy,
                topBar = {
                    Column {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "Travium Admin", style = TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = AdminCardNavy
                            ),
                            actions = {
                                IconButton(onClick = { /* Handle Notifications */ }) {
                                    BadgedBox(
                                        badge = { }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.notification),
                                            contentDescription = "Notifications",
                                            tint = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                    }
                },
                bottomBar = {
                    Column {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                        NavigationBar(
                            containerColor = AdminCardNavy,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                icon = { Icon(painterResource(R.drawable.outline_home_24), contentDescription = "Home") },
                                label = { Text("Home") },
                                selected = selectedIndex == 0,
                                onClick = { selectedIndex = 0 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AdminAccentTeal,
                                    selectedTextColor = AdminAccentTeal,
                                    unselectedIconColor = AdminSoftGray,
                                    unselectedTextColor = AdminSoftGray,
                                    indicatorColor = AdminAccentTeal.copy(alpha = 0.1f)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(painterResource(R.drawable.addbox), contentDescription = "Add Guide") },
                                label = { Text("Add Guide") },
                                selected = selectedIndex == 1,
                                onClick = { selectedIndex = 1 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AdminAccentTeal,
                                    selectedTextColor = AdminAccentTeal,
                                    unselectedIconColor = AdminSoftGray,
                                    unselectedTextColor = AdminSoftGray,
                                    indicatorColor = AdminAccentTeal.copy(alpha = 0.1f)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(painterResource(R.drawable.profile), contentDescription = "Banned List") },
                                label = { Text("Banned") },
                                selected = selectedIndex == 2,
                                onClick = { selectedIndex = 2 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AdminAccentTeal,
                                    selectedTextColor = AdminAccentTeal,
                                    unselectedIconColor = AdminSoftGray,
                                    unselectedTextColor = AdminSoftGray,
                                    indicatorColor = AdminAccentTeal.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when(selectedIndex) {
                        0 -> AdminDashboardScreen(title = "Home Dashboard")
                        1 -> AdminDashboardScreen(title = "Add New Guide")
                        2 -> AdminDashboardScreen(title = "Banned Users List")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AdminDeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen(title = "Welcome Admin")
}
