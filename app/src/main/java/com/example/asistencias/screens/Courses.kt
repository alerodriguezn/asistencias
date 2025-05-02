package com.example.asistencias.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.asistencias.data.Course
import com.example.asistencias.ui.components.CourseCard
import com.google.firebase.firestore.FirebaseFirestore



@Composable
fun CourseManagementScreen(onAddNew: () -> Unit, onEditItem: (String) -> Unit) {


    val courses = remember { mutableStateListOf<Course>() }
    val db = FirebaseFirestore.getInstance()
    val TAG = "CourseManagementScreen"

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
                    .padding(start = 16.dp, top = 30.dp, end = 16.dp, bottom = 16.dp),
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
                    .padding(8.dp)
                ,

                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Buscar Curso") },
                    modifier = Modifier.weight(1f),

                )
                Spacer(modifier = Modifier.width(8.dp))

            }

            CourseList(courses = courses, onEditItem = onEditItem)
        }
    }
}

@Composable
fun CourseList(courses: List<Course>, onEditItem: (String) -> Unit) {
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
    ){
        items(courses) { course ->
            CourseCard(course = course, onEditItem = onEditItem)
        }
    }
}

