package com.example.asistencias.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.asistencias.core.navigation.navigateIntelligently
import com.example.asistencias.data.Jornada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaJornadaForm(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("d MMMM, yyyy", Locale("es", "ES"))

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = "Agregar Nueva Jornada",
                    onValueChange = {},
                    enabled = false,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color.White
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            @Composable
            fun customField(
                value: String,
                label: String,
                onValueChange: (String) -> Unit,
                readOnly: Boolean = false,
                trailing: @Composable (() -> Unit)? = null
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label, color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    trailingIcon = trailing,
                    shape = RoundedCornerShape(6.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    enabled = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE6DFEB),
                        unfocusedContainerColor = Color(0xFFE6DFEB),
                        disabledContainerColor = Color(0xFFE6DFEB),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color(0xFF6A5ACD),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            customField(nombre, "Nombre de la Jornada", { nombre = it })
            customField(anio, "Año", { anio = it })

            customField(fechaInicio, "Fecha de Apertura", { fechaInicio = it }, readOnly = true) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Seleccionar fecha",
                    modifier = Modifier.clickable {
                        showDatePicker { fechaInicio = it }
                    }
                )
            }

            customField(fechaFin, "Fecha de Cierre", { fechaFin = it }, readOnly = true) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Seleccionar fecha",
                    modifier = Modifier.clickable {
                        showDatePicker { fechaFin = it }
                    }
                )
            }

            Button(
                onClick = {
                    if (nombre.isBlank() || anio.isBlank() || fechaInicio.isBlank() || fechaFin.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor, completá todos los campos.")
                        }
                    } else {
                        // Calcular semestre
                        val semestre = try {
                            val formato = SimpleDateFormat("d MMMM, yyyy", Locale("es", "ES"))
                            val fechaCierreDate = formato.parse(fechaFin)

                            val calendar = Calendar.getInstance().apply { time = fechaCierreDate }

                            val mes = calendar.get(Calendar.MONTH) + 1  // Enero = 0
                            val dia = calendar.get(Calendar.DAY_OF_MONTH)

                            if (mes < 8 || (mes == 8 && dia == 1)) "I" else "II"
                        } catch (e: Exception) {
                            ""
                        }


                        val docRef = db.collection("jornadas").document()
                        val jornada = hashMapOf(
                            "id" to docRef.id,
                            "nombre" to nombre,
                            "anio" to anio.toIntOrNull(),
                            "semestre" to semestre,
                            "fechaInicio" to fechaInicio,
                            "fechaFin" to fechaFin,
                            "estado" to "Activa"
                        )

                        docRef.set(jornada)
                            .addOnSuccessListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("¡Jornada guardada con éxito!")
                                }
                                navController.navigateIntelligently("JornadasScreen", clearBackStack = true)
                            }
                            .addOnFailureListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al guardar. Inténtalo de nuevo.")
                                }
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar", color = Color.White)
            }
        }
    }
}
