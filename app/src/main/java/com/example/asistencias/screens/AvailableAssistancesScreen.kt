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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.asistencias.data.AssistanceRequest
import com.example.asistencias.data.StudentApplication
import com.example.asistencias.data.User
import com.example.asistencias.ui.components.AvailableAssistanceCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableAssistancesScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    
    val availableAssistances = remember { mutableStateListOf<AssistanceRequest>() }
    val userApplications = remember { mutableStateListOf<StudentApplication>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    
    // Cargar datos del usuario actual
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { firebaseUser ->
            db.collection("usuarios").document(firebaseUser.uid).get()
                .addOnSuccessListener { document ->
                    currentUser = document.toObject(User::class.java)
                }
        }
    }
    
    // Cargar asistencias disponibles (solo las que están activas/pendientes)
    LaunchedEffect(Unit) {
        db.collection("assistance_requests")
            .whereIn("status", listOf("Pendiente", "Activa"))
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                
                if (snapshots != null) {
                    availableAssistances.clear()
                    for (document in snapshots) {
                        val assistance = document.toObject(AssistanceRequest::class.java)
                        if (assistance != null) {
                            availableAssistances.add(assistance)
                        }
                    }
                    isLoading = false
                }
            }
    }
    
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
                        userApplications.clear()
                        for (document in snapshots) {
                            val application = document.toObject(StudentApplication::class.java)
                            if (application != null) {
                                userApplications.add(application)
                            }
                        }
                    }
                }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistencias Disponibles") },
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
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar asistencias...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (availableAssistances.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay asistencias disponibles",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vuelve más tarde para ver nuevas oportunidades",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Filtrar asistencias según la búsqueda
                val filteredAssistances = availableAssistances.filter { assistance ->
                    assistance.assistanceTypeName.contains(searchQuery, ignoreCase = true) ||
                    assistance.courseName.contains(searchQuery, ignoreCase = true) ||
                    assistance.jornadaNombre.contains(searchQuery, ignoreCase = true)
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAssistances) { assistance ->
                        val hasApplied = userApplications.any { it.assistanceRequestId == assistance.id }
                        val application = userApplications.find { it.assistanceRequestId == assistance.id }
                        
                        AvailableAssistanceCard(
                            assistance = assistance,
                            hasApplied = hasApplied,
                            applicationStatus = application?.status,
                            onApplyClick = {
                                navController.navigate("StudentApplicationForm/${assistance.id}")
                            },
                            onViewDetailsClick = {
                                // Aquí podrías navegar a una pantalla de detalles
                            }
                        )
                    }
                }
            }
        }
    }
} 