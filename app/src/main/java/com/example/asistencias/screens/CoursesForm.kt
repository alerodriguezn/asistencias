package com.example.asistencias.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.asistencias.data.Course

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseForm(
    courseId: String? = null,
    viewModel: CourseViewModel = viewModel(),
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {

    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val errors = remember { mutableStateOf(mapOf<String, String>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(courseId) {
        courseId?.let { id ->
            viewModel.editingItemId = id
            viewModel.getCourseById(id) { course ->
                code = course?.code ?: ""
                name = course?.name ?: ""
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(
                    text = if (courseId == null) "Nuevo Curso" else "Editar Curso"
                ) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código del Curso") },
                modifier = Modifier.fillMaxWidth(),
                isError = errors.value.containsKey("code"),
                supportingText = { errors.value["code"]?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Curso") },
                modifier = Modifier.fillMaxWidth(),
                isError = errors.value.containsKey("name"),
                supportingText = { errors.value["name"]?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {

                focusManager.clearFocus()
                errors.value = mapOf()

                if (code.isEmpty()) {
                    errors.value += ("code" to "El código es requerido")
                }
                if (name.isEmpty()) {
                    errors.value += ("name" to "El nombre es requerido")
                }

                if (errors.value.isEmpty()) {
                    val course = Course(
                        code = code,
                        name = name
                    )
                    viewModel.saveCourse(
                        course,
                        onSuccess = onSaveSuccess,
                        onError = { exception ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error al guardar el curso: ${exception.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Curso")
            }
        }
    }
}