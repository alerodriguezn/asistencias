package com.example.asistencias.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.asistencias.core.navigation.navigateBackIntelligently
import com.example.asistencias.data.AssistanceRequest
import com.example.asistencias.data.StudentApplication
import com.example.asistencias.data.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationForm(
    navController: NavHostController,
    assistanceRequestId: String
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para el formulario
    var bankAccount by remember { mutableStateOf("") }
    var weightedAverage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var assistanceRequest by remember { mutableStateOf<AssistanceRequest?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Cargar datos de la asistencia
    LaunchedEffect(assistanceRequestId) {
        db.collection("assistance_requests")
            .document(assistanceRequestId)
            .get()
            .addOnSuccessListener { document ->
                assistanceRequest = document.toObject(AssistanceRequest::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    // Cargar datos del usuario actual
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { firebaseUser ->
            db.collection("usuarios").document(firebaseUser.uid).get()
                .addOnSuccessListener { document ->
                    currentUser = document.toObject(User::class.java)
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Aplicar a Asistencia") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBackIntelligently() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (assistanceRequest == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Asistencia no encontrada")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información de la asistencia
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información de la Asistencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        InfoRow("Tipo de Asistencia", assistanceRequest!!.assistanceTypeName)
                        InfoRow("Jornada", assistanceRequest!!.jornadaNombre)
                        InfoRow("Curso", assistanceRequest!!.courseName)
                        InfoRow("Profesor", assistanceRequest!!.professorName)
                        
                        if (assistanceRequest!!.comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Comentarios: ${assistanceRequest!!.comments}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Información del estudiante
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Mi Información",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        currentUser?.let { user ->
                            InfoRow("Nombre", "${user.nombre} ${user.apellido1} ${user.apellido2}")
                            InfoRow("Carné", user.carnet)
                            InfoRow("Carrera", user.carrera)
                            InfoRow("Correo", user.correo)
                        }
                    }
                }

                // Cuenta Bancaria
                OutlinedTextField(
                    value = bankAccount,
                    onValueChange = { bankAccount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cuenta Bancaria *") },
                    placeholder = { Text("Ingresa tu número de cuenta bancaria...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A5ACD),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                // Promedio Ponderado
                OutlinedTextField(
                    value = weightedAverage,
                    onValueChange = { weightedAverage = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Promedio Ponderado *") },
                    placeholder = { Text("Ingresa tu promedio ponderado (ej: 8.5)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A5ACD),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de envío
                Button(
                    onClick = {
                        if (bankAccount.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor ingresa tu cuenta bancaria")
                            }
                            return@Button
                        }

                        if (weightedAverage.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor ingresa tu promedio ponderado")
                            }
                            return@Button
                        }

                        if (currentUser == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: No se pudo obtener la información del usuario")
                            }
                            return@Button
                        }

                        val application = StudentApplication(
                            id = db.collection("student_applications").document().id,
                            assistanceRequestId = assistanceRequestId,
                            studentId = auth.currentUser?.uid ?: "",
                            studentName = "${currentUser!!.nombre} ${currentUser!!.apellido1} ${currentUser!!.apellido2}",
                            studentEmail = currentUser!!.correo,
                            studentCarnet = currentUser!!.carnet,
                            studentCarrera = currentUser!!.carrera,
                            motivation = "",
                            bankAccount = bankAccount,
                            weightedAverage = weightedAverage
                        )

                        db.collection("student_applications")
                            .document(application.id)
                            .set(application)
                            .addOnSuccessListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Aplicación enviada exitosamente")
                                    navController.navigateBackIntelligently()
                                }
                            }
                            .addOnFailureListener { exception ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al enviar: ${exception.message}")
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(
                        "Enviar Aplicación",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
} 