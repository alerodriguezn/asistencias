package com.example.asistencias.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.asistencias.data.Jornada
import com.example.asistencias.ui.components.JornadaCard
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun JornadasScreen(
    navController: NavController,
    navigateToNuevaJornada: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val TAG = "JornadasScreen"
    val jornadas = remember { mutableStateListOf<Jornada>() }

    // Leer los datos desde Firebase
    LaunchedEffect(Unit) {
        db.collection("jornadas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error escuchando jornadas: ${e.message}")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    jornadas.clear()
                    for (doc in it.documents) {
                        try {
                            val jornada = doc.toObject(Jornada::class.java)
                            if (jornada != null) {
                                jornadas.add(jornada)
                            }
                        } catch (ex: Exception) {
                            Log.e(TAG, "Error parseando jornada: ${ex.message}")
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 30.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gestión de Jornadas", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = navigateToNuevaJornada,
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A5ACD),
                        contentColor = Color.White

                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Nueva Jornada", color = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Buscar Jornada") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        enabled = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            items(jornadas) { jornada ->
                JornadaCard(jornada = jornada, onEditClick = {
                    navController.navigate("EditarJornadaForm/${jornada.id}")
                }
                )
            }
        }
    }
}
