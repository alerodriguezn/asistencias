package com.example.asistencias.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.asistencias.HomeScreen
import com.example.asistencias.LoginScreen
import com.example.asistencias.data.Jornada
import com.example.asistencias.screens.AssistanceTypes
import com.example.asistencias.screens.EditarJornadaForm
import com.example.asistencias.screens.JornadasScreen
import com.example.asistencias.screens.NewAssistanceForm
import com.example.asistencias.screens.NuevaJornadaForm
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun NavigationWrapper() {
    // Este es el navController que se pasa a cada pantalla que lo necesita
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "JornadasScreen"
    ) {
        // Pantalla de login
        composable("Login") {
            LoginScreen {
                navController.navigate("Home")
            }
        }

        // Pantalla principal
        composable("Home") {
            HomeScreen {
                navController.navigate("Login")
            }
        }

        // Formulario de asistencia nuevo
        composable("NewAssistanceForm") {
            NewAssistanceForm()
        }

        // Tipos de asistencia
        composable("AssistanceTypes") {
            AssistanceTypes {
                navController.navigate("NewAssistanceForm")
            }
        }

        // Listado de jornadas
        composable("JornadasScreen") {
            JornadasScreen(
                navController = navController,
                navigateToNuevaJornada = {
                    navController.navigate("NuevaJornadaForm")
                }
            )
        }

        // Pantalla para crear nueva jornada
        composable("NuevaJornadaForm") {
            NuevaJornadaForm(navController)
        }

        // Pantalla para editar jornada con ID recibido por argumento
        composable(
            route = "EditarJornadaForm/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val jornadaId = backStackEntry.arguments?.getString("id") ?: ""
            val db = FirebaseFirestore.getInstance()
            var jornada by remember { mutableStateOf<Jornada?>(null) }

            // Buscar jornada desde Firebase una sola vez
            LaunchedEffect(jornadaId) {
                db.collection("jornadas").document(jornadaId).get()
                    .addOnSuccessListener { document ->
                        val jornadaData = document.toObject(Jornada::class.java)
                        jornada = jornadaData
                    }
            }

            // Mostrar pantalla solo si la jornada se cargó correctamente
            jornada?.let {
                EditarJornadaForm(navController = navController, jornadaOriginal = it)
            } ?: run {
                // Puedes mostrar un loader o un mensaje temporal si querés
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
