package com.example.asistencias.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asistencias.auth.PreferencesManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(onLogout: () -> Unit = {}) {
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
    val uid = user?.uid ?: return
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var isEditing by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        val doc = db.collection("usuarios").document(uid).get().await()
        userData = doc.data?.mapValues { it.value.toString() } ?: emptyMap()
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var nombre by remember { mutableStateOf(userData["nombre"].orEmpty()) }
    var apellido1 by remember { mutableStateOf(userData["apellido1"].orEmpty()) }
    var apellido2 by remember { mutableStateOf(userData["apellido2"].orEmpty()) }
    var correo by remember { mutableStateOf(user.email ?: "") }
    var carnet by remember { mutableStateOf(userData["carnet"].orEmpty()) }
    var cedula by remember { mutableStateOf(userData["cedula"].orEmpty()) }
    var carrera by remember { mutableStateOf(userData["carrera"].orEmpty()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perfil de Usuario", fontSize = 20.sp)

        EditableField("Nombre", nombre, isEditing, onValueChange = { nombre = it })
        EditableField("Primer Apellido", apellido1, isEditing, onValueChange = { apellido1 = it })
        EditableField("Segundo Apellido", apellido2, isEditing, onValueChange = { apellido2 = it })
        EditableField("Correo Institucional", correo, isEditing, onValueChange = { correo = it })
        EditableField("Carnet", carnet, isEditing, onValueChange = { carnet = it }, KeyboardType.Number)
        EditableField("Cédula", cedula, isEditing, onValueChange = { cedula = it }, KeyboardType.Number)
        EditableField("Carrera", carrera, isEditing, onValueChange = { carrera = it })

        if (isEditing) {
            ProfileFieldWithEditIcon("Contraseña", "************") {
                showPasswordDialog = true
            }
        } else {
            ProfileField("Contraseña", "************")
        }


        if (showPasswordDialog) {
            var passwordError by remember { mutableStateOf<String?>(null) }

            PasswordChangeDialog(
                errorMessage = passwordError,
                onDismiss = {
                    showPasswordDialog = false
                    passwordError = null
                },
                onConfirm = { current, new, confirm ->
                    scope.launch {
                        if (new != confirm) {
                            passwordError = "Las nuevas contraseñas no coinciden."
                            return@launch
                        }

                        try {
                            val credential = EmailAuthProvider.getCredential(user.email ?: "", current)
                            user.reauthenticate(credential).await()
                            user.updatePassword(new).await()
                            showPasswordDialog = false
                            passwordError = null
                            println("✅ Contraseña cambiada correctamente.")
                        } catch (e: Exception) {
                            passwordError = "La contraseña actual es incorrecta o ocurrió un error."
                            println("❌ Error: ${e.message}")
                        }
                    }
                }
            )
        }



        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    if (isEditing) {
                        db.collection("usuarios").document(uid).update(
                            mapOf(
                                "nombre" to nombre,
                                "apellido1" to apellido1,
                                "apellido2" to apellido2,
                                "correo" to correo,
                                "carnet" to carnet,
                                "cedula" to cedula,
                                "carrera" to carrera
                            )
                        )
                        isEditing = false
                    } else {
                        isEditing = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D478B))
        ) {
            Text(if (isEditing) "Guardar Cambios" else "Editar", color = Color.White)
        }

        Button(
            onClick = {
                Firebase.auth.signOut()
                prefs.setRememberMeState(false)
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
        ) {
            Text("Cerrar sesión", color = Color.White)
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        db.collection("usuarios").document(uid).delete().await()
                        Firebase.auth.currentUser?.delete()?.await()
                        prefs.setRememberMeState(false)
                        onLogout()
                    } catch (e: Exception) {
                        println("❌ Error eliminando cuenta: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2))
        ) {
            Text("Darse de baja en el sistema", color = Color.White)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
@Composable
fun ProfileField(label: String, value: String) {
    Column {
        Text(label, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1EAF4)),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                focusedContainerColor = Color(0xFFF1EAF4),
                unfocusedContainerColor = Color(0xFFF1EAF4),
                disabledContainerColor = Color(0xFFF1EAF4),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ProfileFieldWithEditIcon(label: String, value: String, onEditClick: () -> Unit) {
    Column {
        Text(label, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1EAF4)),
            trailingIcon = {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar contraseña")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Gray,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF1EAF4),
                unfocusedContainerColor = Color(0xFFF1EAF4),
                disabledContainerColor = Color(0xFFF1EAF4)
            )
        )
    }
}

@Composable
fun PasswordChangeDialog(
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(currentPassword, newPassword, confirmPassword)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Cambiar Contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.Red)
                }
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña actual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar nueva contraseña") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        }
    )
}


@Composable
fun EditableField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 14.sp)
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEditing,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1EAF4)),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = {
                if (isEditing) Icon(Icons.Default.Edit, contentDescription = "Editar")
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                focusedContainerColor = Color(0xFFF1EAF4),
                unfocusedContainerColor = Color(0xFFF1EAF4),
                disabledContainerColor = Color(0xFFF1EAF4),
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

