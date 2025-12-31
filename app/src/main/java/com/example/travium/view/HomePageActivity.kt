package com.example.travium.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.travium.repository.ChatRepo
import com.example.travium.repository.ChatRepoImpl
import com.example.travium.ui.chat.ChatListScreen

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBody(){

    data class NavItems(val label : String, val icon: Int)
    val chatRepo: ChatRepo = ChatRepoImpl()
    val currentUserId = "user123" // replace with actual userId
    var selectedIndex by remember { mutableStateOf(0) }

    var search by remember { mutableStateOf("") }


    val listItems = listOf(

            NavItems("Home", R.drawable.outline_home_24),

            NavItems("Guide", R.drawable.outline_map_pin_review_24),
            NavItems("Post", R.drawable.addbox),
            NavItems("ChatBox", icon = R.drawable.chatbox),
            NavItems("Profile", R.drawable.profile),
        )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Travium", style = TextStyle(
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.LightGray
                ),
                actions = {
                    Icon(painter = painterResource(R.drawable.notification),
                        contentDescription = null
                    )


                    Spacer(modifier = Modifier.width(20.dp))



                }
            )
        },

        bottomBar = {
            NavigationBar {
                listItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(painter = painterResource(item.icon),
                                contentDescription = item.label)
                        },
                        label = {Text(item.label)},
                        selected = selectedIndex == index,
                        onClick = {selectedIndex=index}
                    )
                }

            }

        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            Box(modifier = Modifier
                .fillMaxSize()
            ){
                when(selectedIndex){
                    0 -> HomeScreenBody()
                    1 -> HomeScreenBody()
                    2 -> MakePostBody()
                    3 -> ChatFeatureScreen(chatRepo = chatRepo, currentUserId = currentUserId)

                    else -> HomeScreenBody()
                }

            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun HomePreviewer(){
    HomeBody()
}
