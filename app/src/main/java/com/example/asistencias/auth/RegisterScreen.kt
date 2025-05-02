package com.example.asistencias.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navigateToLogin: () -> Unit = {}
) {
    var correo by remember { mutableStateOf("") }
    var apellido1 by remember { mutableStateOf("") }
    var apellido2 by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var carnet by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var carrera by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()), // 🔁 Habilita el scroll
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text("Formulario de Registro", fontSize = 22.sp, color = Color(0xFF5D478B))
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apellido1, onValueChange = { apellido1 = it },
            label = { Text("Primer Apellido") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apellido2, onValueChange = { apellido2 = it },
            label = { Text("Segundo Apellido") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = carnet, onValueChange = { carnet = it },
            label = { Text("Carnet") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cedula, onValueChange = { cedula = it },
            label = { Text("Cédula") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = carrera, onValueChange = { carrera = it },
            label = { Text("Carrera") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo Institucional") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = contrasena, onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmarContrasena, onValueChange = { confirmarContrasena = it },
            label = { Text("Confirme la Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    errorMessage = null
                    successMessage = null

                    if (contrasena != confirmarContrasena) {
                        errorMessage = "Las contraseñas no coinciden"
                        return@launch
                    }

                    val result = AuthManager.registerWithEmail(
                        email = correo,
                        password = contrasena,
                        nombre = nombre,
                        apellido1 = apellido1,
                        apellido2 = apellido2,
                        carnet = carnet,
                        cedula = cedula,
                        carrera = carrera
                    )

                    if (result == null) {
                        successMessage = "Registro exitoso. Se ha enviado un correo de verificación a $correo. Revisa tu bandeja de entrada."
                    } else {
                        errorMessage = result
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D478B))
        ) {
            Text("Registrarse", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }

        successMessage?.let {
            Text(text = it, color = Color.Green)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = navigateToLogin,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2))
            ) {
                Text("Ir al inicio de sesión", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (successMessage == null) {
            TextButton(onClick = { navigateToLogin() }) {
                Text("¿Ya tienes cuenta? Inicia Sesión", color = Color.Blue)
            }
        }
    }
}
