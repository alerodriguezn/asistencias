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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.asistencias.auth.AuthManager
import com.example.asistencias.auth.LoginScreen
import com.example.asistencias.auth.PreferencesManager
import com.example.asistencias.auth.RegisterScreen
import com.example.asistencias.profile.ProfileScreen
import com.example.asistencias.screens.CourseForm
import com.example.asistencias.screens.CourseManagementScreen
import com.example.asistencias.screens.NewAssistanceForm
import com.example.asistencias.screens.NuevaJornadaForm
import com.google.firebase.firestore.FirebaseFirestore




sealed class Routes(val route: String) {
    data object Login : Routes("Login")
    data object Register : Routes("Register")
    data object Profile : Routes("Profile")
    data object Home : Routes("Home")
    data object NuevaJornadaForm: Routes("NuevaJornadaForm")
    
    data object AssistanceTypes : Routes("AssistanceTypes")
    data object Courses : Routes("Courses")
    data object NewAssistanceForm : Routes("NewAssistanceForm") {
        fun withId(id: String): String {
            return "$route/$id"
        }
    }
    data object CoursesForm : Routes("CoursesForm") {
        fun withId(id: String): String {
            return "$route/$id"
        }
    }
}

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

    NavHost(
        navController = navController,
        startDestination = startDestination = startDestination
    ) {
      
      // Pantalla para crear nueva jornada
        composable(Routes.NuevaJornadaForm.route) {
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

        
        composable(Routes.Login.route) {
            LoginScreen(
                navigateToHome = { navController.navigate(Profile) },
                navigateToRegister = { navController.navigate(Register) }
            )
        }

        composable(Routes.Register.route){
            RegisterScreen(
                navigateToLogin = { navController.navigate(Login) }
            )
        }

        composable(Routes.Profile.route){
            ProfileScreen(
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo(Profile) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen {
                navController.navigate(Routes.Login.route) {
                    popUpTo(Routes.Home.route) { inclusive = true }
                }
            }
        }

        composable(Routes.Courses.route){
            CourseManagementScreen(
                onAddNew = {
                    navController.navigate(Routes.CoursesForm.route)
                },
                onEditItem = { itemId ->
                    navController.navigate(Routes.CoursesForm.withId(itemId))
                }
            )
        }

        // Route with parameters
        composable(
            route = "${Routes.CoursesForm.route}/{itemId}",
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            CourseForm(
                courseId = itemId,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        // Route without parameters
        composable(Routes.CoursesForm.route) {
            CourseForm(
                courseId = null,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.AssistanceTypes.route) {
            AssistanceTypes(
                onAddNew = {
                    navController.navigate(Routes.NewAssistanceForm.route) // Navigate without ID
                },
                onEditItem = { itemId ->
                    navController.navigate(Routes.NewAssistanceForm.withId(itemId))
                }
            )
        }

        // Route without parameters
        composable(Routes.NewAssistanceForm.route) {
            NewAssistanceForm(
                itemId = null,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Route with parameters
        composable(
            route = "${Routes.NewAssistanceForm.route}/{itemId}",
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            NewAssistanceForm(
                itemId = itemId,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}


