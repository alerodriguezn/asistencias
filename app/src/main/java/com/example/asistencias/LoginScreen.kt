package com.example.asistencias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp


@Composable
fun LoginScreen(navigateToHome : () -> Unit){
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally){
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Login Screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            navigateToHome()
        }){
            Text(text = "Go to Home")
        }
        Spacer(modifier = Modifier.weight(1f))

    }
}