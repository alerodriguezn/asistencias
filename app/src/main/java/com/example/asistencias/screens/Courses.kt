package com.example.asistencias.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.asistencias.data.Course
import com.example.asistencias.data.User
import com.example.asistencias.ui.components.CourseCard
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CourseManagementScreen(onAddNew: () -> Unit, onEditItem: (String) -> Unit) {
    val courses = remember { mutableStateListOf<Course>() }
    val db = FirebaseFirestore.getInstance()
    val TAG = "CourseManagementScreen"
    var searchQuery by remember { mutableStateOf("") }
    var showProfessorDialog by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(Unit) {
        db.collection("courses")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening to changes: ", exception)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    courses.clear()
                    for (document in snapshots) {
                        val course = document.toObject(Course::class.java)
                        courses.add(course)
                        println(course.name)
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
                Text("Gestión de Cursos", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = {
                    onAddNew()
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Add"
                    )
                    Text("Nuevo Curso", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar Curso") },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            val filteredCourses = courses.filter { course ->
                course.name.contains(searchQuery, ignoreCase = true)
            }
            
            CourseList(
                courses = filteredCourses,
                onEditItem = onEditItem,
                onManageProfessors = { course ->
                    selectedCourse = course
                    showProfessorDialog = true
                }
            )
        }
    }

    if (showProfessorDialog && selectedCourse != null) {
        ProfessorManagementDialog(
            course = selectedCourse!!,
            onDismiss = { showProfessorDialog = false },
            onSave = { updatedCourse ->
                db.collection("courses")
                    .document(updatedCourse.id)
                    .set(updatedCourse)
                    .addOnSuccessListener {
                        showProfessorDialog = false
                    }
            }
        )
    }
}

@Composable
fun ProfessorManagementDialog(
    course: Course,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var professors by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedProfessors by remember { mutableStateOf(course.professorIds) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("usuarios")
            .whereEqualTo("rol", "Profesor")
            .get()
            .addOnSuccessListener { documents ->
                professors = documents.mapNotNull { it.toObject(User::class.java) }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestionar Profesores - ${course.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar Profesor") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val filteredProfessors = professors.filter { professor ->
                        "${professor.nombre} ${professor.apellido1} ${professor.apellido2}"
                            .contains(searchQuery, ignoreCase = true)
                    }
                    
                    items(filteredProfessors) { professor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedProfessors.contains(professor.cedula),
                                onCheckedChange = { checked ->
                                    selectedProfessors = if (checked) {
                                        selectedProfessors + professor.cedula
                                    } else {
                                        selectedProfessors - professor.cedula
                                    }
                                }
                            )
                            Text(
                                text = "${professor.nombre} ${professor.apellido1} ${professor.apellido2}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(course.copy(professorIds = selectedProfessors))
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CourseList(
    courses: List<Course>,
    onEditItem: (String) -> Unit,
    onManageProfessors: (Course) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
    ) {
        items(courses) { course ->
            CourseCard(
                course = course,
                onEditItem = onEditItem,
                onManageProfessors = { onManageProfessors(course) }
            )
        }
    }
}

