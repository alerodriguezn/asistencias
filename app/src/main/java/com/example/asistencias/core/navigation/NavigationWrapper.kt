package com.example.asistencias.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.asistencias.HomeScreen
import com.example.asistencias.LoginScreen
import com.example.asistencias.screens.AssistanceTypes
import com.example.asistencias.screens.NewAssistanceForm
import com.example.asistencias.screens.JornadasScreen
import com.example.asistencias.screens.NuevaJornadaForm

@Composable
fun NavigationWrapper() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "JornadasScreen") {

        composable("Login") {
            LoginScreen {
                navController.navigate("Home")
            }
        }

        composable("Home") {
            HomeScreen {
                navController.navigate("Login")
            }
        }

        composable("NewAssistanceForm") {
            NewAssistanceForm()
        }

        composable("AssistanceTypes") {
            AssistanceTypes {
                navController.navigate("NewAssistanceForm")
            }
        }

        composable("JornadasScreen") {
            JornadasScreen(
                navigateToNuevaJornada = {
                    navController.navigate("NuevaJornadaForm")
                }
            )
        }

        composable("NuevaJornadaForm") {
            NuevaJornadaForm(navController)
        }
    }
}
