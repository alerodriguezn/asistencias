package com.example.asistencias.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val dominiosPermitidos = listOf(
        "@itcr.ac.cr",
        "@estudiantec.cr",
        "@tec.ac.cr"
    )

    private fun isEmailAllowed(email: String): Boolean {
        return dominiosPermitidos.any { email.endsWith(it, ignoreCase = true) }
    }

    suspend fun loginWithEmail(email: String, password: String): String? {
        return try {
            if (!isEmailAllowed(email)) {
                return "Solo se permiten correos institucionales del TEC."
            }

            auth.signInWithEmailAndPassword(email, password).await()

            if (auth.currentUser?.isEmailVerified == false) {
                auth.signOut()
                return "Debes verificar tu correo antes de iniciar sesión."
            }

            null
        } catch (e: Exception) {
            when {
                e.message?.contains("password is invalid", ignoreCase = true) == true ||
                        e.message?.contains("auth credential", ignoreCase = true) == true -> {
                    "La contraseña es incorrecta."
                }
                e.message?.contains("no user record", ignoreCase = true) == true -> {
                    "No existe una cuenta con este correo."
                }
                else -> e.message ?: "Error desconocido al iniciar sesión."
            }
        }
    }


    suspend fun registerWithEmail(
        email: String,
        password: String,
        nombre: String,
        apellido1: String,
        apellido2: String,
        carnet: String,
        cedula: String,
        carrera: String
    ): String? {
        return try {
            if (!isEmailAllowed(email)) {
                return "Solo se permiten correos institucionales del TEC."
            }

            auth.createUserWithEmailAndPassword(email, password).await()
            auth.currentUser?.sendEmailVerification()?.await()

            val uid = auth.currentUser?.uid ?: return "Error al obtener ID del usuario."

            val usuario = mapOf(
                "correo" to email,
                "nombre" to nombre,
                "apellido1" to apellido1,
                "apellido2" to apellido2,
                "carnet" to carnet,
                "cedula" to cedula,
                "carrera" to carrera
            )

            // Carga segura de Firestore (sin memory leaks)
            val firestore = Firebase.firestore
            firestore.collection("usuarios").document(uid).set(usuario).await()

            null
        } catch (e: Exception) {
            e.message ?: "Error desconocido al registrarse."
        }
    }

    fun resetPassword(email: String, onResult: (String?) -> Unit) {
        if (email.isBlank()) {
            onResult("Por favor, ingresa tu correo institucional.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(null)
                } else {
                    onResult(task.exception?.message ?: "Error al enviar el correo.")
                }
            }
    }
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

}


