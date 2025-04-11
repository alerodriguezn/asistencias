package com.example.asistencias.screens


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.asistencias.data.Assistance
import com.example.asistencias.ui.components.AssistantShipListItem
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AssistanceTypes(navigateToNewAssistance: () -> Unit) {
    val assistantShips = remember { mutableStateListOf<Assistance>() }
    val db = FirebaseFirestore.getInstance()
    val TAG = "AssistanceTypes"


    LaunchedEffect(Unit) {
        db.collection("assistances")
            .get()
            .addOnSuccessListener { result ->
                assistantShips.clear()
                for (document in result) {
                    val assistance = document.toObject(Assistance::class.java)
                    assistantShips.add(assistance)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching assistances: ", exception)
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp).padding(
            top = 4.dp
        ),
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tipo de Asistencias",
                        style = MaterialTheme.typography.titleLarge
                    )
                    FilledTonalButton(onClick = {
                        navigateToNewAssistance()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Add"
                        )
                        Text("Nueva Asistencia", color = MaterialTheme.colorScheme.primary)
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().fillMaxWidth()
                ) {
                    items(assistantShips) { assistantShip ->
                        AssistantShipListItem(assistantShip)
                    }
                }
            }
        }
    )
}