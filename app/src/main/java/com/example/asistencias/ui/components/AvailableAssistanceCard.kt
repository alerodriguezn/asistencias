package com.example.asistencias.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asistencias.data.AssistanceRequest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AvailableAssistanceCard(
    assistance: AssistanceRequest,
    hasApplied: Boolean,
    applicationStatus: String?,
    onApplyClick: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetailsClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con tipo de asistencia y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assistance.assistanceTypeName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                
                // Estado de la aplicación si ya aplicó
                if (hasApplied && applicationStatus != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (applicationStatus) {
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
                            text = applicationStatus,
                            color = Color.White,
                            fontSize = 14.sp,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de la asistencia
            Column(modifier = Modifier.fillMaxWidth()) {
                InfoRow("Jornada", assistance.jornadaNombre)
                InfoRow("Curso", assistance.courseName)
                InfoRow("Profesor", assistance.professorName)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha de creación
            Text(
                text = "Publicada: ${dateFormat.format(assistance.requestDate.toDate())}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Comentarios si existen
            if (assistance.comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Comentarios: ${assistance.comments}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de acción
            if (!hasApplied) {
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aplicar",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aplicar a esta Asistencia",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Si ya aplicó, mostrar información adicional
                OutlinedButton(
                    onClick = onViewDetailsClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A5ACD)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Ver detalles",
                        tint = Color(0xFF6A5ACD)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ver detalles de mi aplicación",
                        color = Color(0xFF6A5ACD),
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