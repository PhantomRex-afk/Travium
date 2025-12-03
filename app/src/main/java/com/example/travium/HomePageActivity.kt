package com.example.travium

import android.R.attr.onClick
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travium.ui.theme.TraviumTheme

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeBody()
        }
    }
}

@Composable
fun HomeBody(){
    Scaffold {
        padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,


                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
                    .background(color = Color.Green)
            ) {


                Text(
                    "Travium", style = TextStyle(
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold
                    )
                )


                Spacer(modifier = Modifier.width(160.dp))


                Icon(painter = painterResource(R.drawable.notification),
                    contentDescription = null
                )


                Spacer(modifier = Modifier.width(20.dp))


                Icon(painter = painterResource(R.drawable.chatbox),
                    contentDescription = null
                )

            }



            Column(
                modifier = Modifier
                    .height(700.dp)
            ) {

            }



            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,


                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(color = Color.LightGray)
                    .height(40.dp)
            ) {


                NavigationRow()


            }
        }
    }
}



@Composable
fun NavigationRow(){
    val context = LocalContext.current

    val activity = context as Activity




    IconButton(onClick = { mark = !mark }) {
        Icon(painter = if (mark){painterResource(R.drawable.outline_home_24)
        } else painterResource(R.drawable.outline_home_24),
            tint = if (mark) Color.Blue else Color.Black,
            contentDescription = null,
        )
        val intent = Intent(context, HomePageActivity::class.java)
        context.startActivity(intent)
        activity.finish()
    }



    Icon(painter = painterResource(R.drawable.addbox),
        contentDescription = null,
        modifier = Modifier.clickable{
            val intent = Intent(context, MakePostActivity::class.java)
            context.startActivity(intent)
            activity.finish()
        }
    )



    Icon(painter = painterResource(R.drawable.outline_map_pin_review_24),
        contentDescription = null
    )



    Icon(painter = painterResource(R.drawable.profile),
        contentDescription = null
    )


}


@Preview(showBackground = true)
@Composable
fun HomePreviewer(){
    HomeBody()
}
