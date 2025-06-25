package com.example.asistencias.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.asistencias.data.StudentApplication
import com.example.asistencias.data.AssistanceRequest
import com.example.asistencias.ui.components.ApplicationReviewDialog
import com.example.asistencias.ui.components.StudentApplicationCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationsManagementScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    
    val applications = remember { mutableStateListOf<StudentApplication>() }
    val assistanceRequests = remember { mutableStateListOf<AssistanceRequest>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var filterStatus by remember { mutableStateOf("Todos") }
    var showReviewDialog by remember { mutableStateOf(false) }
    var selectedApplication by remember { mutableStateOf<StudentApplication?>(null) }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    
    // Cargar todas las aplicaciones
    LaunchedEffect(Unit) {
        db.collection("student_applications")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                
                if (snapshots != null) {
                    applications.clear()
                    for (document in snapshots) {
                        val application = document.toObject(StudentApplication::class.java)
                        if (application != null) {
                            applications.add(application)
                        }
                    }
                    isLoading = false
                }
            }
    }
    
    // Cargar información de las asistencias
    LaunchedEffect(Unit) {
        db.collection("assistance_requests")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                
                if (snapshots != null) {
                    assistanceRequests.clear()
                    for (document in snapshots) {
                        val assistance = document.toObject(AssistanceRequest::class.java)
                        if (assistance != null) {
                            assistanceRequests.add(assistance)
                        }
                    }
                }
            }
    }
    
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            // Filtros y búsqueda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Filtro por estado
                ExposedDropdownMenuBox(
                    expanded = isStatusDropdownExpanded,
                    onExpandedChange = { isStatusDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = filterStatus,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Filtrar por estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = isStatusDropdownExpanded,
                        onDismissRequest = { isStatusDropdownExpanded = false }
                    ) {
                        listOf("Todos", "Pendiente", "Aprobada", "Rechazada").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = { 
                                    filterStatus = status
                                    isStatusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nombre o carnet de estudiante...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    singleLine = true
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (applications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay aplicaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Los estudiantes aún no han aplicado a asistencias",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Filtrar aplicaciones
                val filteredApplications = applications.filter { application ->
                    val matchesSearch = application.studentName.contains(searchQuery, ignoreCase = true) ||
                                       application.studentCarnet.contains(searchQuery, ignoreCase = true)
                    val matchesStatus = filterStatus == "Todos" || application.status == filterStatus
                    matchesSearch && matchesStatus
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredApplications) { application ->
                        val assistance = assistanceRequests.find { it.id == application.assistanceRequestId }
                        
                        StudentApplicationCard(
                            application = application,
                            assistance = assistance,
                            onReviewClick = {
                                selectedApplication = application
                                showReviewDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de revisión
    if (showReviewDialog && selectedApplication != null) {
        ApplicationReviewDialog(
            application = selectedApplication!!,
            assistance = assistanceRequests.find { it.id == selectedApplication!!.assistanceRequestId },
            onDismiss = {
                showReviewDialog = false
                selectedApplication = null
            },
            onApprove = { comments ->
                updateApplicationStatus(selectedApplication!!.id, "Aprobada", comments, auth.currentUser?.uid, auth.currentUser?.email)
                showReviewDialog = false
                selectedApplication = null
            },
            onReject = { comments ->
                updateApplicationStatus(selectedApplication!!.id, "Rechazada", comments, auth.currentUser?.uid, auth.currentUser?.email)
                showReviewDialog = false
                selectedApplication = null
            }
        )
    }
}

private fun updateApplicationStatus(
    applicationId: String,
    status: String,
    comments: String,
    reviewerId: String?,
    reviewerEmail: String?
) {
    val db = FirebaseFirestore.getInstance()
    val updateData = hashMapOf<String, Any>(
        "status" to status,
        "reviewDate" to Timestamp.now(),
        "reviewerId" to (reviewerId ?: ""),
        "reviewerName" to (reviewerEmail ?: "Administrador"),
        "comments" to comments
    )
    
    db.collection("student_applications")
        .document(applicationId)
        .update(updateData)
        .addOnSuccessListener {
            // Aquí podrías enviar una notificación al estudiante
        }
        .addOnFailureListener { exception ->
            // Manejar error
        }
} 