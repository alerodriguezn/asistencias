package com.example.asistencias.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.asistencias.auth.AuthManager
import com.example.asistencias.auth.LoginScreen
import com.example.asistencias.auth.PreferencesManager
import com.example.asistencias.auth.RegisterScreen
import com.example.asistencias.profile.ProfileScreen
import com.example.asistencias.screens.AssistanceTypes
import com.example.asistencias.screens.NewAssistanceForm


@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    val startDestination = remember {
        if (AuthManager.isUserLoggedIn() && prefs.getRememberMeState()) {
            Profile
        } else {
            Login
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable<Login> {
            LoginScreen(
                navigateToHome = { navController.navigate(Profile) },
                navigateToRegister = { navController.navigate(Register) }
            )
        }

        composable<Register> {
            RegisterScreen(
                navigateToLogin = { navController.navigate(Login) }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo(Profile) { inclusive = true }
                    }
                }
            )
        }


        composable<NewAssistanceForm> {
            NewAssistanceForm()
        }

        composable<AssistanceTypes> {
            AssistanceTypes {
                navController.navigate(NewAssistanceForm)
            }
        }
    }
}
