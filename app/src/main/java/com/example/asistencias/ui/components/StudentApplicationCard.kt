package com.example.asistencias.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asistencias.data.StudentApplication
import com.example.asistencias.data.AssistanceRequest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentApplicationCard(
    application: StudentApplication,
    assistance: AssistanceRequest?,
    onReviewClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReviewClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con información del estudiante y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.studentName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Text(
                        text = "Carné: ${application.studentCarnet}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                // Estado de la aplicación
                Box(
                    modifier = Modifier
                        .background(
                            color = when (application.status) {
                                "Pendiente" -> Color(0xFFFF9800)
                                "Aprobada" -> Color(0xFF4CAF50)
                                "Rechazada" -> Color(0xFFF44336)
                                else -> Color(0xFF9E9E9E)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = application.status,
                        color = Color.White,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de la asistencia
            assistance?.let { assist ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    InfoRow("Asistencia", assist.assistanceTypeName)
                    InfoRow("Jornada", assist.jornadaNombre)
                    InfoRow("Curso", assist.courseName)
                    InfoRow("Profesor", assist.professorName)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información del estudiante
            Column(modifier = Modifier.fillMaxWidth()) {
                InfoRow("Carrera", application.studentCarrera)
                InfoRow("Correo", application.studentEmail)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aplicó: ${dateFormat.format(application.applicationDate.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (application.reviewDate != null) {
                    Text(
                        text = "Revisada: ${dateFormat.format(application.reviewDate.toDate())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Comentarios del revisor si existen
            if (application.comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Comentarios: ${application.comments}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de revisión (solo para aplicaciones pendientes)
            if (application.status == "Pendiente") {
                Button(
                    onClick = onReviewClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                ) {
                    Icon(
                        imageVector = Icons.Default.Reviews,
                        contentDescription = "Revisar",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Revisar Aplicación",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (application.status == "Aprobada") {
                // Solo mostrar "Ver Detalles" para aplicaciones aprobadas
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Revisada por: ${application.reviewerName ?: "Administrador"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onReviewClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6A5ACD)
                        )
                    ) {
                        Text("Ver Detalles")
                    }
                }
            } else {
                // Para aplicaciones rechazadas, solo mostrar información del revisor
                Text(
                    text = "Revisada por: ${application.reviewerName ?: "Administrador"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
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