package com.example.travium.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travium.R

@Composable
fun HomeScreenBody() {


    var search by remember { mutableStateOf("") }

    Column {


        Spacer(modifier = Modifier.height(30.dp))
        OutlinedTextField(
            value = search,
            onValueChange = {
                search = it
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.clickable(onClick = {})
                )
            },
            placeholder = {
                Text("Search")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            )
        )


        Spacer(modifier = Modifier.height(40.dp))


        Row {
            Text("Caption", modifier = Modifier.padding(10.dp))

        }



        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(start = 20.dp, end = 30.dp)
                .fillMaxWidth()
                .height(400.dp)
        ) {


        }

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(10.dp)
            ) {
            Icon(painter = painterResource(R.drawable.heart),
                contentDescription = null,
                modifier = Modifier.clickable(onClick = {})
                    .size(40.dp)
            )

            Text("Like")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Icon(painter = painterResource(R.drawable.comment),
                    contentDescription = null,modifier = Modifier.clickable(onClick = {})
                        .size(40.dp)
                )

                Text("Comment")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreviewer(){
    HomeScreenBody()
}