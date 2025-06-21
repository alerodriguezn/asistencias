package com.example.asistencias.comunicados

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.asistencias.data.Comunicado




@Composable
fun ComunicadosAdminScreen(
    onCrearNuevo: () -> Unit = {},
    onEditar: (String) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    var comunicados by remember { mutableStateOf<List<Comunicado>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filtroTitulo by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("todos") }
    val opcionesEstado = listOf("todos", "activo", "inactivo")
    var expanded by remember { mutableStateOf(false) }
    var filtroFecha by remember { mutableStateOf("") }
    val context = LocalContext.current
    val formatter = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year: Int, month: Int, day: Int ->
                calendar.set(year, month, day)
                onDateSelected(formatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }



    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("comunicados").get().await()
            comunicados = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comunicado::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            // Manejo de errores (toast, snackbar, etc.)
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = filtroTitulo,
            onValueChange = { filtroTitulo = it },
            label = { Text("Filtrar por título") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = filtroEstado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Estado") },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                opcionesEstado.forEach { estado ->
                    DropdownMenuItem(
                        text = { Text(estado.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            filtroEstado = estado
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { showDatePicker { filtroFecha = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Filtrar por fecha de publicación: ${if (filtroFecha.isEmpty()) "Seleccionar" else filtroFecha}")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                filtroTitulo = ""
                filtroEstado = "todos"
                filtroFecha = ""
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Limpiar filtros")
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCrearNuevo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Agregar Nuevo Comunicado")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            val comunicadosFiltrados = comunicados.filter {
                it.titulo.contains(filtroTitulo, ignoreCase = true) &&
                        (filtroEstado == "todos" || it.estado.equals(filtroEstado, ignoreCase = true)) &&
                        (filtroFecha.isEmpty() || it.fechaPublicacion == filtroFecha)
            }


            LazyColumn {
                items(comunicadosFiltrados) { comunicado ->
                    ComunicadoCardAdmin(
                        comunicado = comunicado,
                        onEditar = onEditar,
                        onEliminar = { id ->
                            db.collection("comunicados").document(id).delete()
                                .addOnSuccessListener {
                                    comunicados = comunicados.filterNot { it.id == id }
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ComunicadoCardAdmin(
    comunicado: Comunicado,
    onEditar: (String) -> Unit,
    onEliminar: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(comunicado.titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Descripción: ${comunicado.descripcion}")
            Text("Fecha de Publicación: ${comunicado.fechaPublicacion}")
            Text("Fecha de Cierre: ${comunicado.fechaCierre}")
            Text("Estado: ${comunicado.estado}")

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { onEditar(comunicado.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { onEliminar(comunicado.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

