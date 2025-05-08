package com.example.asistencias.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asistencias.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navigateToHome: () -> Unit,
    navigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var recordar by remember { mutableStateOf(prefs.getRememberMeState()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo TEC",
            modifier = Modifier.height(100.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Institucional") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Confirme la Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(15.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = recordar,
                onCheckedChange = {
                    recordar = it
                    prefs.setRememberMeState(it)
                }
            )
            Text("Recuérdame")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = {
                scope.launch {
                    AuthManager.resetPassword(correo) { result ->
                        errorMessage = result ?: "Se envió un correo para restablecer la contraseña."
                    }
                }
            }
        ) {
            Text("¿Olvidó su contraseña?", color = Color.Blue)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tiene cuenta?")
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = navigateToRegister) {
                Text("Regístrese", color = Color.Blue)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    val result = AuthManager.loginWithEmail(correo, contrasena)
                    if (result == null) {
                        if (recordar) prefs.setRememberMeState(true)
                        navigateToHome()
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
            Text("Iniciar Sesión", color = Color.White, fontSize = 16.sp)
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = it, color = if (it.contains("envió")) Color.Green else Color.Red, fontSize = 14.sp)
        }
    }
}
