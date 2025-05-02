package com.example.asistencias.screens



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditRole(
    user: User,
    onDismiss: () -> Unit,
    onRoleChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // Controla la visibilidad del menú
    var selectedRole by remember { mutableStateOf(user.rol) } // Rol seleccionado
    val roles = listOf("Estudiante", "Profesor", "Administrador") // Opciones de rol

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Editar Rol") },
        text = {
            Column {
                Text("Cambiar el rol de ${user.nombre} ${user.apellido1} ${user.apellido2}")
                Spacer(modifier = Modifier.height(8.dp))

                // Menú desplegable para seleccionar el rol
                Box {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedRole.ifEmpty { "Seleccionar Rol" })
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onRoleChange(selectedRole)
                onDismiss()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}