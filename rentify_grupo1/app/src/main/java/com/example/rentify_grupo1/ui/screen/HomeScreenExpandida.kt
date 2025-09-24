package com.example.rentify_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rentify_grupo1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenExpandida() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi App Kotlin") })
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column(
                modifier = Modifier.weight(weight = 1f)
            ){
                Text(text="modo expandido", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(height = 16.dp))
            }
            Image(
                painter = painterResource(id = R.drawable.sdfaerg),
                contentDescription = "Logo App",
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Preview(name="Xpanded", widthDp = 1000, heightDp = 800)
@Composable
fun HomeScreenExpandidaPreview(){
    HomeScreenExpandida()
}