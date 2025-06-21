package com.example.asistencias.comunicados

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.asistencias.notifications.NotificationManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ComunicadoForm(
    comunicadoId: String? = null,
    onSaved: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notificationManager = remember { NotificationManager(context) }

    var titulo by remember { mutableStateOf(TextFieldValue()) }
    var descripcion by remember { mutableStateOf(TextFieldValue()) }
    var fechaPublicacion by remember { mutableStateOf("") }
    var fechaCierre by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("activo") }
    val opcionesEstado = listOf("activo", "inactivo")
    var expandedEstado by remember { mutableStateOf(false) }

    val isEditing = comunicadoId != null
    val formatter = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

    LaunchedEffect(comunicadoId) {
        if (isEditing) {
            db.collection("comunicados").document(comunicadoId!!).get().addOnSuccessListener { doc ->
                titulo = TextFieldValue(doc.getString("titulo") ?: "")
                descripcion = TextFieldValue(doc.getString("descripcion") ?: "")
                fechaPublicacion = doc.getString("fechaPublicacion") ?: ""
                fechaCierre = doc.getString("fechaCierre") ?: ""
                estado = doc.getString("estado") ?: "activo"
            }
        }
    }

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(year, month, day)
                onDateSelected(formatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título del Comunicado") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { showDatePicker { fechaPublicacion = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fecha de Publicación: ${if (fechaPublicacion.isEmpty()) "Seleccionar" else fechaPublicacion}")
        }

        Button(
            onClick = { showDatePicker { fechaCierre = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fecha de Cierre: ${if (fechaCierre.isEmpty()) "Seleccionar" else fechaCierre}")
        }

        // 👉 Selector de estado
        Box {
            OutlinedTextField(
                value = estado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Estado del Comunicado") },
                trailingIcon = {
                    IconButton(onClick = { expandedEstado = !expandedEstado }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expandedEstado,
                onDismissRequest = { expandedEstado = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                opcionesEstado.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            estado = opcion
                            expandedEstado = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                val comunicado = hashMapOf(
                    "titulo" to titulo.text,
                    "descripcion" to descripcion.text,
                    "fechaPublicacion" to fechaPublicacion,
                    "fechaCierre" to fechaCierre,
                    "estado" to estado // 👈 usamos el valor seleccionado
                )

                scope.launch {
                    if (isEditing) {
                        db.collection("comunicados").document(comunicadoId!!).set(comunicado)
                            .addOnSuccessListener { onSaved() }
                    } else {
                        db.collection("comunicados").add(comunicado)
                            .addOnSuccessListener { 
                                // Enviar notificación solo cuando se crea un nuevo comunicado
                                if (estado == "activo") {
                                    notificationManager.createComunicadoNotification(
                                        titulo.text,
                                        descripcion.text
                                    )
                                }
                                onSaved() 
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = titulo.text.isNotEmpty() && descripcion.text.isNotEmpty()
                    && fechaPublicacion.isNotEmpty() && fechaCierre.isNotEmpty()
        ) {
            Text(if (isEditing) "Actualizar" else "Guardar")
        }
    }
}
