package com.example.asistencias.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tune
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
    val todasJornadas = remember { mutableStateListOf<Jornada>() }
    val jornadasFiltradas = remember { mutableStateListOf<Jornada>() }

    var showDialog by remember { mutableStateOf(false) }
    var showEstadoDialog by remember { mutableStateOf(false) }
    var jornadaSeleccionada by remember { mutableStateOf<Jornada?>(null) }
    var modoCambioEstado by remember { mutableStateOf("") }

    var filtroValor by remember { mutableStateOf("") }
    var expandedFiltro by remember { mutableStateOf(false) }

// Diálogo de selección de semestre/año
    var mostrarDialogoSemestre by remember { mutableStateOf(false) }
    var mostrarDialogoAnio by remember { mutableStateOf(false) }

// Años únicos desde Firebase
    val opcionesAnios = remember { mutableStateListOf<String>() }

    // Función para aplicar el filtro actual
    fun aplicarFiltro() {
        jornadasFiltradas.clear()
        if (filtroValor.isBlank()) {
            jornadasFiltradas.addAll(todasJornadas)
        } else {
            if (filtroValor.startsWith("Semestre")) {
                val semestre = filtroValor.removePrefix("Semestre ").trim()
                jornadasFiltradas.addAll(todasJornadas.filter { it.semestre == semestre })
            } else {
                jornadasFiltradas.addAll(todasJornadas.filter { it.anio.toString() == filtroValor })
            }
        }
    }

// Escucha en tiempo real de Firestore
    LaunchedEffect(Unit) {
        db.collection("jornadas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error escuchando jornadas: ${e.message}")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    todasJornadas.clear()
                    val aniosUnicos = mutableSetOf<Int>()

                    for (doc in it.documents) {
                        val jornada = doc.toObject(Jornada::class.java)
                        if (jornada != null) {
                            todasJornadas.add(jornada)
                            aniosUnicos.add(jornada.anio)
                        }
                    }

                    opcionesAnios.clear()
                    opcionesAnios.addAll(aniosUnicos.sortedDescending().map { it.toString() })

                    aplicarFiltro()
                }
            }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 15.dp, end = 16.dp, bottom = 16.dp),
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
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filtroValor,
                    onValueChange = {},
                    placeholder = { Text("Buscar Jornada") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(onClick = { expandedFiltro = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filtrar Jornadas")
                    }

                    DropdownMenu(
                        expanded = expandedFiltro,
                        onDismissRequest = { expandedFiltro = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Filtrar por Semestre") },
                            onClick = {
                                expandedFiltro = false
                                mostrarDialogoSemestre = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Filtrar por Año") },
                            onClick = {
                                expandedFiltro = false
                                mostrarDialogoAnio = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mostrar todas las Jornadas") },
                            onClick = {
                                expandedFiltro = false
                                filtroValor = ""
                                aplicarFiltro()
                            }
                        )
                    }
                }
            }
            // Lista de jornadas filtradas
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jornadasFiltradas) { jornada ->
                    JornadaCard(
                        jornada = jornada,
                        onEditClick = {
                            navController.navigate("EditarJornadaForm/${jornada.id}")
                        },
                        onDeleteClick = {
                            jornadaSeleccionada = jornada
                            showDialog = true
                        },
                        onToggleEstado = {
                            jornadaSeleccionada = jornada
                            modoCambioEstado = if (jornada.estado == "Activa") "Finalizar" else "Activar"
                            showEstadoDialog = true
                        }
                    )
                }
            }

            // Diálogo eliminar
            if (showDialog && jornadaSeleccionada != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmación") },
                    text = {
                        Column {
                            Text("¿Estás seguro que deseas eliminar \"${jornadaSeleccionada?.nombre}\"?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("¡CUIDADO!", color = Color.Red)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                jornadaSeleccionada?.let { jornada ->
                                    db.collection("jornadas").document(jornada.id)
                                        .delete()
                                        .addOnSuccessListener { showDialog = false }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Sí", color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("No, Cancelar.", color = Color.White)
                        }
                    }
                )
            }

            // Diálogo cambio de estado
            if (showEstadoDialog && jornadaSeleccionada != null) {
                val nuevoEstado = if (jornadaSeleccionada!!.estado == "Activa") "Finalizada" else "Activa"
                AlertDialog(
                    onDismissRequest = { showEstadoDialog = false },
                    title = { Text("Confirmación") },
                    text = {
                        Text("¿Estás seguro que deseas $modoCambioEstado la jornada \"${jornadaSeleccionada!!.nombre}\"?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                db.collection("jornadas")
                                    .document(jornadaSeleccionada!!.id)
                                    .update("estado", nuevoEstado)
                                    .addOnSuccessListener {
                                        showEstadoDialog = false
                                        jornadaSeleccionada = null
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Sí", color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showEstadoDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("No", color = Color.White)
                        }
                    }
                )
            }

            // Diálogo para seleccionar semestre
            if (mostrarDialogoSemestre) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoSemestre = false },
                    title = { Text("¿Por cuál semestre deseas filtrar la búsqueda?") },
                    confirmButton = {},
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    filtroValor = "Semestre I"
                                    aplicarFiltro()
                                    mostrarDialogoSemestre = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Semestre I")
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    filtroValor = "Semestre II"
                                    aplicarFiltro()
                                    mostrarDialogoSemestre = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Semestre II")
                            }
                        }
                    }
                )
            }

            // Diálogo para seleccionar año
            if (mostrarDialogoAnio) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoAnio = false },
                    title = { Text("¿Por cuál año deseas filtrar la búsqueda?") },
                    confirmButton = {},
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            opcionesAnios.forEach { anio ->
                                Button(
                                    onClick = {
                                        filtroValor = anio
                                        aplicarFiltro()
                                        mostrarDialogoAnio = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(anio)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
