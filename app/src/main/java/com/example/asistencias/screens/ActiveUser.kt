package com.example.asistencias.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmToggleActiveDialog(
    user: User,
    isActive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirmar acción") },
        text = {
            Text(
                if (isActive)
                    "¿Estás seguro de que deseas activar a ${user.nombre} ${user.apellido1}?"
                else
                    "¿Estás seguro de que deseas desactivar a ${user.nombre} ${user.apellido1}?"
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}