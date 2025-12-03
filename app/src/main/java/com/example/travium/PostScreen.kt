package com.example.travium

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MakePostBody(){

    val context = LocalContext.current

    val activity = context as Activity




    Scaffold { padding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,


                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(10.dp)
                    .background(color = Color.Gray)
            ) {


                Icon(painter = painterResource(R.drawable.cancel),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp).clickable{
                        val intent = Intent(context, HomePageActivity::class.java)
                        context.startActivity(intent)
                        activity.finish()
                    }
                )

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    "Create a Post", style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Column(
                modifier = Modifier
                    .height(700.dp)
            ) {
                Card(

                    modifier = Modifier
                        .padding(10.dp)
                        .height(400.dp)
                        .fillMaxWidth()

                ) {

                    Icon(painter = painterResource(R.drawable.image),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterHorizontally)
                            .size(60.dp)
                    )

                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)

                ) {
                    Text(
                        "Add a caption", style = TextStyle(
                            fontSize = 25.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .padding(10.dp)

                            .fillMaxWidth()
                            .height(30.dp)
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)

                ){


                    Text("Add location", style = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                        modifier = Modifier
                            .padding(10.dp)

                            .fillMaxWidth()
                            .height(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()

                        .height(60.dp).padding(horizontal = 15.dp)
                ) {
                    Text("Post", style = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                        modifier = Modifier
                            .padding(10.dp)

                            .fillMaxWidth()
                            .height(30.dp))
                }

            }

            Spacer(modifier = Modifier.height(25.dp))



        }
    }
}