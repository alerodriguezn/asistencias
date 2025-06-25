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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.asistencias.core.navigation.Routes
import com.example.asistencias.data.StudentApplication
import com.example.asistencias.data.AssistanceRequest
import com.example.asistencias.ui.components.StudentApplicationCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    
    val myApplications = remember { mutableStateListOf<StudentApplication>() }
    val assistanceRequests = remember { mutableStateListOf<AssistanceRequest>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var filterStatus by remember { mutableStateOf("Todos") }
    
    // Cargar aplicaciones del usuario actual
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { user ->
            db.collection("student_applications")
                .whereEqualTo("studentId", user.uid)
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        return@addSnapshotListener
                    }
                    
                    if (snapshots != null) {
                        myApplications.clear()
                        for (document in snapshots) {
                            val application = document.toObject(StudentApplication::class.java)
                            if (application != null) {
                                myApplications.add(application)
                            }
                        }
                        isLoading = false
                    }
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
        topBar = {
            TopAppBar(
                title = { Text("Mis Aplicaciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros y búsqueda
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtro por estado
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = filterStatus,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = { }
                    ) {
                        listOf("Todos", "Pendiente", "Aprobada", "Rechazada").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = { filterStatus = status }
                            )
                        }
                    }
                }
                
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(2f),
                    placeholder = { Text("Buscar por asistencia...") },
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
            } else if (myApplications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tienes aplicaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aplica a asistencias disponibles para verlas aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                navController.navigate(Routes.AvailableAssistances.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Ver Asistencias Disponibles", color = Color.White)
                        }
                    }
                }
            } else {
                // Filtrar aplicaciones
                val filteredApplications = myApplications.filter { application ->
                    val assistance = assistanceRequests.find { it.id == application.assistanceRequestId }
                    val matchesSearch = assistance?.assistanceTypeName?.contains(searchQuery, ignoreCase = true) == true ||
                                       assistance?.courseName?.contains(searchQuery, ignoreCase = true) == true
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
                                // Para estudiantes, solo mostrar detalles
                                // No permitir editar
                            }
                        )
                    }
                }
            }
        }
    }
} 