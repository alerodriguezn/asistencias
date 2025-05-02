package com.example.asistencias.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.asistencias.data.Assistance
import com.example.asistencias.screens.AssistanceViewModel

@Composable
fun AssistantShipListItem(
    assistance: Assistance,
    onEditItem: (String) -> Unit,
    viewModel: AssistanceViewModel = viewModel(),
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp)
                    .weight(1f),
            ) {
                Text(assistance.name.trim(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(assistance.requirements.trim(), fontSize = 14.sp, fontStyle = FontStyle.Italic)
                Text(assistance.benefits.trim(), fontSize = 14.sp, fontStyle = FontStyle.Italic)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            menuExpanded = false
                            onEditItem(assistance.id)
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            menuExpanded = false
                            showDialog = true // Show confirmation dialog
                        }
                    )
                }
            }
        }
    }

    // Confirmation Dialog
    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta asistencia?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.getAssistanceById(assistance.id) { assistanceToDelete ->
                            if (assistanceToDelete != null) {
                                viewModel.deleteAssistance(assistanceToDelete.id) {
                                    // Handle any additional UI updates if needed
                                }
                            }
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth(),
        thickness = 1.dp,
        color = Color.Gray
    )
}