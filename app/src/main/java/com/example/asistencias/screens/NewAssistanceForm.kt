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
import com.example.asistencias.data.Assistance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAssistanceForm(
    itemId: String? = null,
    viewModel: AssistanceViewModel = viewModel(),
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {

    var name by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var benefits by remember { mutableStateOf("") }

    val errors = remember { mutableStateOf(mapOf<String, String>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(itemId) {
        itemId?.let { id ->
            viewModel.editingItemId = id
            viewModel.getAssistanceById(id) { assistance ->
                name = assistance?.name ?: ""
                requirements = assistance?.requirements ?: ""
                benefits = assistance?.benefits ?: ""
            }

        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(
                    text = if (itemId == null) "Nueva Asistencia" else "Editar Asistencia"
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre Asistencia") },
                modifier = Modifier.fillMaxWidth(),
                isError = errors.value.containsKey("name"),
                supportingText = { errors.value["name"]?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = requirements,
                onValueChange = { requirements = it },
                label = { Text("Requerimientos") },
                modifier = Modifier.fillMaxWidth(),
                isError = errors.value.containsKey("requirements"),
                supportingText = { errors.value["requirements"]?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = benefits,
                onValueChange = { benefits = it },
                label = { Text("Beneficios") },
                modifier = Modifier.fillMaxWidth(),
                isError = errors.value.containsKey("benefits"),
                supportingText = { errors.value["benefits"]?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {

                focusManager.clearFocus()
                errors.value = mapOf()

                if (name.isEmpty()) {
                    errors.value += ("name" to "El nombre es requerido")
                }
                if (requirements.isEmpty()) {
                    errors.value += ("requirements" to "Los requerimientos son requeridos")
                }
                if (benefits.isEmpty()) {
                    errors.value += ("benefits" to "Los beneficios son requeridos")
                }

                if (errors.value.isEmpty()) {
                    val assistance = Assistance(
                        name = name,
                        requirements = requirements,
                        benefits = benefits
                    )
                    viewModel.saveAssistance(
                        assistance,
                        onSuccess = onSaveSuccess,

                        onError = { exception ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error al guardar la asistencia: ${exception.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            },
                modifier = Modifier.fillMaxWidth()
                ) {
                Text("Guardar Asistencia")
            }
        }
    }
}