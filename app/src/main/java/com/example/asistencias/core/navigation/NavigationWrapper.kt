package com.example.asistencias.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.asistencias.HomeScreen
import com.example.asistencias.LoginScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.asistencias.auth.AuthManager
import com.example.asistencias.auth.LoginScreen
import com.example.asistencias.auth.PreferencesManager
import com.example.asistencias.auth.RegisterScreen
import com.example.asistencias.profile.ProfileScreen
import com.example.asistencias.screens.AssistanceTypes
import com.example.asistencias.screens.CourseForm
import com.example.asistencias.screens.CourseManagementScreen
import com.example.asistencias.screens.NewAssistanceForm



sealed class Routes(val route: String) {
    data object Login : Routes("Login")
    data object Register : Routes("Register")
    data object Profile : Routes("Profile")
    data object Home : Routes("Home")
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

