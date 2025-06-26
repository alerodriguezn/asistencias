package com.example.asistencias.screens

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.asistencias.data.AssistanceRequest
import com.example.asistencias.notifications.NotificationManager
import com.example.asistencias.ui.components.AssistanceRequestCard
import com.example.asistencias.ui.components.AssistanceRequestReviewDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AssistanceRequestsScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val notificationManager = remember { NotificationManager(context) }
    
    val requests = remember { mutableStateListOf<AssistanceRequest>() }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Solicitudes de Asistencia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { navController.navigate("AssistanceRequestForm") }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar solicitudes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true
            )

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
                        },
                        onReviewClick = {
                            selectedRequest = request
                            showReviewDialog = true
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

    // Diálogo de revisión
    if (showReviewDialog && selectedRequest != null) {
        AssistanceRequestReviewDialog(
            request = selectedRequest!!,
            onDismiss = {
                showReviewDialog = false
                selectedRequest = null
            },
            onApprove = { comments ->
                updateAssistanceRequestStatus(
                    selectedRequest!!.id,
                    "Aprobada",
                    comments,
                    auth.currentUser?.uid,
                    auth.currentUser?.email,
                    notificationManager
                )
                showReviewDialog = false
                selectedRequest = null
            },
            onReject = { comments ->
                updateAssistanceRequestStatus(
                    selectedRequest!!.id,
                    "Rechazada",
                    comments,
                    auth.currentUser?.uid,
                    auth.currentUser?.email,
                    notificationManager
                )
                showReviewDialog = false
                selectedRequest = null
            }
        )
    }
}

private fun updateAssistanceRequestStatus(
    requestId: String,
    status: String,
    comments: String,
    reviewerId: String?,
    reviewerEmail: String?,
    notificationManager: NotificationManager
) {
    val db = FirebaseFirestore.getInstance()
    
    val updateData = hashMapOf<String, Any>(
        "status" to status,
        "reviewDate" to Timestamp.now(),
        "reviewerId" to (reviewerId ?: ""),
        "reviewerName" to (reviewerEmail ?: "Administrador"),
        "comments" to comments
    )
    
    db.collection("assistance_requests")
        .document(requestId)
        .update(updateData)
        .addOnSuccessListener {
            // Obtener la solicitud actualizada para enviar notificación
            db.collection("assistance_requests")
                .document(requestId)
                .get()
                .addOnSuccessListener { document ->
                    val request = document.toObject(AssistanceRequest::class.java)
                    if (request != null) {
                        // Enviar notificación al creador de la solicitud
                        notificationManager.createAssistanceRequestStatusNotification(
                            requestId = requestId,
                            assistanceName = request.assistanceTypeName,
                            status = status,
                            comments = comments,
                            studentId = request.createdBy,
                            studentName = request.createdByName,
                            reviewerName = reviewerEmail ?: "Administrador"
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AssistanceRequestsScreen", "Error obteniendo solicitud actualizada", exception)
                }
        }
        .addOnFailureListener { exception ->
            Log.e("AssistanceRequestsScreen", "Error actualizando solicitud", exception)
        }
} 