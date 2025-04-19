package com.example.asistencias.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.asistencias.data.Jornada

@Composable
fun JornadaCard(jornada: Jornada, onEditClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6FF)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(jornada.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    jornada.estado,
                    color = if (jornada.estado == "Activa") Color.Blue else Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Año: ${jornada.anio}")
                Text("Semestre: ${jornada.semestre}")
            }

            Spacer(Modifier.height(4.dp))

            Text("Inicio: ${jornada.fechaInicio}")
            Text("Fin: ${jornada.fechaFin}")

            // Ícono de editar (siempre visible)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar jornada",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
