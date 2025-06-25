package com.example.asistencias.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.asistencias.data.AssistanceRequest
import com.example.asistencias.ui.components.AssistanceRequestCard
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AssistanceRequestsScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val requests = remember { mutableStateListOf<AssistanceRequest>() }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<AssistanceRequest?>(null) }
    var filterStatus by remember { mutableStateOf("Todos") }

    // Cargar solicitudes
    LaunchedEffect(Unit) {
        db.collection("assistance_requests")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    requests.clear()
                    for (document in snapshots) {
                        val request = document.toObject(AssistanceRequest::class.java)
                        if (request != null) {
                            requests.add(request)
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
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Solicitudes de Asistencia", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(
                    onClick = {
                        navController.navigate("AssistanceRequestForm")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Add"
                    )
                    Text("Nueva Solicitud", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Barra de búsqueda y filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar solicitudes...") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de solicitudes
            val filteredRequests = requests.filter { request ->
                val matchesSearch = request.jornadaNombre.contains(searchQuery, ignoreCase = true) ||
                        request.courseName.contains(searchQuery, ignoreCase = true) ||
                        request.professorName.contains(searchQuery, ignoreCase = true) ||
                        request.assistanceTypeName.contains(searchQuery, ignoreCase = true)
                
                val matchesStatus = filterStatus == "Todos" || request.status == filterStatus
                
                matchesSearch && matchesStatus
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRequests) { request ->
                    AssistanceRequestCard(
                        request = request,
                        onEditClick = {
                            navController.navigate("AssistanceRequestForm/${request.id}")
                        },
                        onDeleteClick = {
                            selectedRequest = request
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && selectedRequest != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { 
                Text("¿Estás seguro de que deseas eliminar esta solicitud de asistencia?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedRequest?.let { request ->
                            db.collection("assistance_requests")
                                .document(request.id)
                                .delete()
                                .addOnSuccessListener {
                                    showDeleteDialog = false
                                    selectedRequest = null
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 