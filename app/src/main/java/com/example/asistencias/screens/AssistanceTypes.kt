package com.example.asistencias.screens


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.asistencias.data.Assistance
import com.example.asistencias.ui.components.AssistantShipListItem
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AssistanceTypes(onAddNew: () -> Unit, onEditItem: (String) -> Unit) {
    val assistantShips = remember { mutableStateListOf<Assistance>() }
    val db = FirebaseFirestore.getInstance()
    val TAG = "AssistanceTypes"
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    LaunchedEffect(Unit) {
        db.collection("assistances")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening to changes: ", exception)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    assistantShips.clear()
                    for (document in snapshots) {
                        val assistance = document.toObject(Assistance::class.java)
                        assistantShips.add(assistance)
                    }
                }
            }

    }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        floatingActionButton = {
            // Mostrar FAB en pantallas pequeñas
            if (screenWidth < 600) {
                FloatingActionButton(
                    onClick = onAddNew,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar nueva asistencia"
                    )
                }
            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con título y botón (solo en pantallas medianas/grandes)
                if (screenWidth >= 600) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tipos de Asistencias",
                            style = MaterialTheme.typography.titleLarge
                        )
                        FilledTonalButton(
                            onClick = onAddNew,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Nueva Asistencia")
                        }
                    }
                } else {
                    // Solo título en pantallas pequeñas
                    Text(
                        text = "Tipos de Asistencias",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (screenWidth < 600) 80.dp else 16.dp
                    )
                ) {
                    items(assistantShips) { assistantShip ->
                        AssistantShipListItem(assistantShip, onEditItem)
                    }
                }
            }
        }
    )
}