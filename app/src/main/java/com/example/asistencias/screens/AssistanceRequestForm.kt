package com.example.asistencias.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.asistencias.core.navigation.navigateBackIntelligently
import com.example.asistencias.data.AssistanceRequest
import com.example.asistencias.data.Assistance
import com.example.asistencias.data.Course
import com.example.asistencias.data.Jornada
import com.example.asistencias.data.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistanceRequestForm(
    navController: NavHostController,
    requestId: String? = null
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para los datos del formulario
    var selectedJornada by remember { mutableStateOf<Jornada?>(null) }
    var selectedAssistanceType by remember { mutableStateOf<Assistance?>(null) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedProfessor by remember { mutableStateOf<User?>(null) }
    var comments by remember { mutableStateOf("") }

    // Estados para los diálogos de selección
    var showJornadaDialog by remember { mutableStateOf(false) }
    var showAssistanceTypeDialog by remember { mutableStateOf(false) }
    var showCourseDialog by remember { mutableStateOf(false) }
    var showProfessorDialog by remember { mutableStateOf(false) }

    // Estados para las listas de datos
    var jornadas by remember { mutableStateOf<List<Jornada>>(emptyList()) }
    var assistanceTypes by remember { mutableStateOf<List<Assistance>>(emptyList()) }
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var professors by remember { mutableStateOf<List<User>>(emptyList()) }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        // Cargar jornadas activas
        db.collection("jornadas")
            .whereEqualTo("estado", "Activa")
            .get()
            .addOnSuccessListener { documents ->
                jornadas = documents.mapNotNull { it.toObject(Jornada::class.java) }
            }

        // Cargar tipos de asistencia
        db.collection("assistances")
            .get()
            .addOnSuccessListener { documents ->
                assistanceTypes = documents.mapNotNull { it.toObject(Assistance::class.java) }
            }

        // Cargar cursos
        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                courses = documents.mapNotNull { it.toObject(Course::class.java) }
            }

        // Cargar profesores
        db.collection("usuarios")
            .whereEqualTo("rol", "Profesor")
            .get()
            .addOnSuccessListener { documents ->
                professors = documents.mapNotNull { it.toObject(User::class.java) }
            }
    }

    // Cargar datos si es edición
    LaunchedEffect(requestId) {
        requestId?.let { id ->
            db.collection("assistance_requests")
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    document?.toObject(AssistanceRequest::class.java)?.let { request ->
                        selectedJornada = jornadas.find { it.id == request.jornadaId }
                        selectedAssistanceType = assistanceTypes.find { it.id == request.assistanceTypeId }
                        selectedCourse = courses.find { it.id == request.courseId }
                        selectedProfessor = professors.find { it.cedula == request.professorId }
                        comments = request.comments
                    }
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (requestId == null) "Nueva Solicitud de Asistencia" else "Editar Solicitud") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBackIntelligently() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selección de Jornada
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Jornada",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showJornadaDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text(
                            text = selectedJornada?.nombre ?: "Seleccionar Jornada",
                            color = Color.White
                        )
                    }
                }
            }

            // Selección de Tipo de Asistencia
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tipo de Asistencia",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAssistanceTypeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text(
                            text = selectedAssistanceType?.name ?: "Seleccionar Tipo de Asistencia",
                            color = Color.White
                        )
                    }
                }
            }

            // Selección de Curso
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Curso",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showCourseDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text(
                            text = selectedCourse?.name ?: "Seleccionar Curso",
                            color = Color.White
                        )
                    }
                }
            }

            // Selección de Profesor
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Profesor",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showProfessorDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                        enabled = selectedCourse != null
                    ) {
                        Text(
                            text = selectedProfessor?.let { "${it.nombre} ${it.apellido1} ${it.apellido2}" } ?: "Seleccionar Profesor",
                            color = Color.White
                        )
                    }
                    if (selectedCourse != null && selectedProfessor != null) {
                        val isProfessorAssigned = selectedCourse!!.professorIds.contains(selectedProfessor!!.cedula)
                        if (!isProfessorAssigned) {
                            Text(
                                text = "⚠️ Este profesor no está asignado al curso seleccionado",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Comentarios
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Comentarios (Opcional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Agregar comentarios adicionales...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            // Botón de guardar
            Button(
                onClick = {
                    if (selectedJornada == null || selectedAssistanceType == null || 
                        selectedCourse == null || selectedProfessor == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor, complete todos los campos requeridos")
                        }
                        return@Button
                    }

                    // Verificar que el profesor esté asignado al curso
                    if (!selectedCourse!!.professorIds.contains(selectedProfessor!!.cedula)) {
                        scope.launch {
                            snackbarHostState.showSnackbar("El profesor seleccionado no está asignado al curso")
                        }
                        return@Button
                    }

                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: Usuario no autenticado")
                        }
                        return@Button
                    }

                    val request = AssistanceRequest(
                        id = requestId ?: db.collection("assistance_requests").document().id,
                        jornadaId = selectedJornada!!.id,
                        jornadaNombre = selectedJornada!!.nombre,
                        assistanceTypeId = selectedAssistanceType!!.id,
                        assistanceTypeName = selectedAssistanceType!!.name,
                        courseId = selectedCourse!!.id,
                        courseName = selectedCourse!!.name,
                        professorId = selectedProfessor!!.cedula,
                        professorName = "${selectedProfessor!!.nombre} ${selectedProfessor!!.apellido1} ${selectedProfessor!!.apellido2}",
                        comments = comments,
                        createdBy = currentUser.uid,
                        createdByName = currentUser.email ?: "Administrador"
                    )

                    db.collection("assistance_requests")
                        .document(request.id)
                        .set(request)
                        .addOnSuccessListener {
                            scope.launch {
                                snackbarHostState.showSnackbar("Solicitud guardada exitosamente")
                                navController.navigateBackIntelligently()
                            }
                        }
                        .addOnFailureListener { exception ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al guardar: ${exception.message}")
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(
                    text = if (requestId == null) "Crear Solicitud" else "Actualizar Solicitud",
                    color = Color.White
                )
            }
        }
    }

    // Diálogo de selección de Jornada
    if (showJornadaDialog) {
        AlertDialog(
            onDismissRequest = { showJornadaDialog = false },
            title = { Text("Seleccionar Jornada") },
            text = {
                LazyColumn {
                    items(jornadas) { jornada ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedJornada = jornada
                                    showJornadaDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = jornada.nombre,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${jornada.anio} - Semestre ${jornada.semestre}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showJornadaDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de selección de Tipo de Asistencia
    if (showAssistanceTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAssistanceTypeDialog = false },
            title = { Text("Seleccionar Tipo de Asistencia") },
            text = {
                LazyColumn {
                    items(assistanceTypes) { assistance ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAssistanceType = assistance
                                    showAssistanceTypeDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = assistance.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = assistance.requirements,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAssistanceTypeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de selección de Curso
    if (showCourseDialog) {
        AlertDialog(
            onDismissRequest = { showCourseDialog = false },
            title = { Text("Seleccionar Curso") },
            text = {
                LazyColumn {
                    items(courses) { course ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCourse = course
                                    selectedProfessor = null // Reset professor selection
                                    showCourseDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = course.code,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCourseDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de selección de Profesor
    if (showProfessorDialog && selectedCourse != null) {
        val availableProfessors = professors.filter { professor ->
            selectedCourse!!.professorIds.contains(professor.cedula)
        }
        
        AlertDialog(
            onDismissRequest = { showProfessorDialog = false },
            title = { Text("Seleccionar Profesor") },
            text = {
                if (availableProfessors.isEmpty()) {
                    Text("No hay profesores asignados a este curso")
                } else {
                    LazyColumn {
                        items(availableProfessors) { professor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedProfessor = professor
                                        showProfessorDialog = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${professor.nombre} ${professor.apellido1} ${professor.apellido2}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfessorDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 