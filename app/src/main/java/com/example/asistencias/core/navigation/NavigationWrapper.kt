package com.example.asistencias.core.navigation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.asistencias.data.Jornada
import com.example.asistencias.screens.AssistanceTypes
import com.example.asistencias.screens.EditarJornadaForm
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.asistencias.auth.AuthManager
import com.example.asistencias.auth.LoginScreen
import com.example.asistencias.auth.PreferencesManager
import com.example.asistencias.auth.RegisterScreen
import com.example.asistencias.profile.ProfileScreen
import com.example.asistencias.screens.CourseForm
import com.example.asistencias.screens.CourseManagementScreen
import com.example.asistencias.screens.NewAssistanceForm
import com.example.asistencias.screens.NuevaJornadaForm
import com.example.asistencias.screens.NotificationsScreen
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.Icon
import com.example.asistencias.comunicados.ComunicadoForm
import com.example.asistencias.comunicados.ComunicadosAdminScreen
import com.example.asistencias.comunicados.ComunicadosInicioScreen
import com.example.asistencias.screens.UserManagementScreen
import com.example.asistencias.screens.JornadasScreen
import com.example.asistencias.screens.AssistanceRequestForm
import com.example.asistencias.screens.AssistanceRequestsScreen
import com.example.asistencias.screens.AvailableAssistancesScreen
import com.example.asistencias.screens.StudentApplicationForm
import com.example.asistencias.screens.StudentApplicationsManagementScreen
import com.example.asistencias.screens.HomeScreen
import com.example.asistencias.screens.MyApplicationsScreen

// Función de utilidad para navegación inteligente
fun NavHostController.navigateIntelligently(route: String, clearBackStack: Boolean = false) {
    val currentRoute = this.currentBackStackEntry?.destination?.route
    
    if (currentRoute == route) {
        // Si ya estamos en la misma pantalla, no hacer nada
        return
    }
    
    this.navigate(route) {
        if (clearBackStack) {
            // Limpiar la pila hasta la pantalla de inicio
            popUpTo(Routes.Home.route) {
                inclusive = false
            }
        }
        // Evitar crear múltiples copias de la misma pantalla
        launchSingleTop = true
        // Restaurar el estado si la pantalla ya existe
        restoreState = true
    }
}

// Función para navegación desde formularios (manejo especial del botón atrás)
fun NavHostController.navigateFromForm(route: String) {
    this.navigate(route) {
        // Para formularios, solo limpiar hasta la pantalla anterior
        // No limpiar toda la pila para mantener el contexto
        launchSingleTop = true
        restoreState = true
    }
}

// Función para volver atrás de manera inteligente
fun NavHostController.navigateBackIntelligently() {
    if (this.previousBackStackEntry != null) {
        this.popBackStack()
    } else {
        // Si no hay pantalla anterior, ir a Home
        this.navigateIntelligently(Routes.Home.route, clearBackStack = true)
    }
}

sealed class Routes(val route: String, val title: String, val imageVector: ImageVector? = null) {
    data object Login : Routes("LoginScreen", "Inicio de Sesión")
    data object Register : Routes("RegisterScreen",     "Registro")
    data object UserManagement : Routes("UserManagement", "Gestionar Usuarios", Icons.Default.Person)
    data object Profile : Routes("ProfileScreen", "Perfil", Icons.Default.AccountCircle)
    data object Home : Routes("Home", "Inicio", Icons.Default.Home)
    data object Jornadas : Routes("JornadasScreen", "Jornadas", Icons.Default.CalendarToday)
    data object NuevaJornadaForm: Routes("NuevaJornadaForm", "Nueva Jornada")
    data object AssistanceTypes : Routes("AssistanceTypes", "Tipos de Asistencias",
        Icons.AutoMirrored.Filled.List
    )
    data object Courses : Routes("Courses", "Cursos", Icons.Default.Info)
    data object Notifications : Routes("Notifications", "Notificaciones", Icons.Default.Notifications)
    data object NewAssistanceForm : Routes("NewAssistanceForm", "Nueva Asistencia") {
        fun withId(id: String): String {
            return "$route/$id"
        }
    }
    data object CoursesForm : Routes("CoursesForm", "Editar Curso") {
        fun withId(id: String): String {
            return "$route/$id"
        }
    }
    data object ComunicadosAdmin : Routes("ComunicadosAdmin", "Gestión de Comunicados", Icons.AutoMirrored.Filled.Send)
    data object ComunicadosInicio : Routes("ComunicadosInicio", "Comunicados Activos", Icons.AutoMirrored.Filled.Send  )
    data object ComunicadoForm : Routes("ComunicadoForm", "Nuevo Comunicado")
    data object AssistanceRequests : Routes("AssistanceRequests", "Solicitudes de Asistencia", Icons.AutoMirrored.Filled.FactCheck)
    data object AssistanceRequestForm : Routes("AssistanceRequestForm", "Nueva Solicitud") {
        fun withId(id: String): String {
            return "$route/$id"
        }
    }
    data object AvailableAssistances : Routes("AvailableAssistances", "Asistencias Disponibles", Icons.Default.Info)
    data object StudentApplicationForm : Routes("StudentApplicationForm", "Aplicar a Asistencia") {
        fun withId(id: String): String {
            return "$route/$id"
        }
    }
    data object StudentApplicationsManagement : Routes("StudentApplicationsManagement", "Gestión de Aplicaciones", Icons.Default.Checklist)
    data object MyApplications : Routes("MyApplications", "Mis Aplicaciones", Icons.Default.Person)
}

@Composable
fun NavigationWrapper(navController: NavHostController) {

//    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    val startDestination = remember {
        if (AuthManager.isUserLoggedIn() && prefs.getRememberMeState()) {
            Routes.Home.route
        } else {
            Routes.Login.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination ,
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

        composable(Routes.UserManagement.route){
            UserManagementScreen()
        }

        
        composable(Routes.Login.route) {
            LoginScreen(
                navigateToHome = { navController.navigate(Routes.Profile.route) },
                navigateToRegister = { navController.navigate(Routes.Register.route) }
            )
        }

        composable(Routes.Register.route){
            RegisterScreen(
                navigateToLogin = { navController.navigate(Routes.Login.route) }
            )
        }

        composable(Routes.Profile.route){
            ProfileScreen(
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Profile.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(navController)
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
                    navController.navigateBackIntelligently()
                }
            )
        }
        // Route without parameters
        composable(Routes.CoursesForm.route) {
            CourseForm(
                courseId = null,
                onSaveSuccess = {
                    navController.navigateBackIntelligently()
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
                    navController.navigateBackIntelligently()
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
                    navController.navigateBackIntelligently()
                }
            )
        }

        // Add Jornadas screen
        composable(Routes.Jornadas.route) {
            JornadasScreen(
                navController = navController,
                navigateToNuevaJornada = {
                    navController.navigate(
                        Routes.NuevaJornadaForm.route
                    )
                }
            )
        }

        // Ruta para pantalla de comunicados (admin)
        composable(Routes.ComunicadosAdmin.route) {
            ComunicadosAdminScreen(
                onCrearNuevo = {
                    navController.navigate(Routes.ComunicadoForm.route)
                },
                onEditar = { comunicadoId ->
                    navController.navigate("${Routes.ComunicadoForm.route}/$comunicadoId")
                }
            )
        }


        // Ruta para pantalla de comunicados activos (usuarios normales)
        composable(Routes.ComunicadosInicio.route) {
            ComunicadosInicioScreen()
        }

// Ruta para editar comunicado
        composable(
            route = "${Routes.ComunicadoForm.route}/{comunicadoId}",
            arguments = listOf(navArgument("comunicadoId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val comunicadoId = backStackEntry.arguments?.getString("comunicadoId")
            ComunicadoForm(
                comunicadoId = comunicadoId,
                onSaved = {
                    navController.navigateBackIntelligently()
                }
            )
        }

        composable(Routes.ComunicadoForm.route) {
            ComunicadoForm(
                comunicadoId = null,
                onSaved = {
                    navController.navigateBackIntelligently()
                }
            )
        }

        composable(Routes.Notifications.route) {
            NotificationsScreen(navController)
        }

        // Rutas para solicitudes de asistencia
        composable(Routes.AssistanceRequests.route) {
            AssistanceRequestsScreen(navController)
        }

        // Route without parameters
        composable(Routes.AssistanceRequestForm.route) {
            AssistanceRequestForm(
                navController = navController,
                requestId = null
            )
        }

        // Route with parameters
        composable(
            route = "${Routes.AssistanceRequestForm.route}/{requestId}",
            arguments = listOf(
                navArgument("requestId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId")
            AssistanceRequestForm(
                navController = navController,
                requestId = requestId
            )
        }

        // Rutas para aplicaciones de estudiantes
        composable(Routes.AvailableAssistances.route) {
            AvailableAssistancesScreen(navController)
        }

        // Route without parameters
        composable(Routes.StudentApplicationForm.route) {
            StudentApplicationForm(
                navController = navController,
                assistanceRequestId = ""
            )
        }

        // Route with parameters
        composable(
            route = "${Routes.StudentApplicationForm.route}/{assistanceRequestId}",
            arguments = listOf(
                navArgument("assistanceRequestId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val assistanceRequestId = backStackEntry.arguments?.getString("assistanceRequestId") ?: ""
            StudentApplicationForm(
                navController = navController,
                assistanceRequestId = assistanceRequestId
            )
        }

        // Ruta para gestión de aplicaciones (admin)
        composable(Routes.StudentApplicationsManagement.route) {
            StudentApplicationsManagementScreen(navController)
        }

        // Ruta para mis aplicaciones (estudiantes)
        composable(Routes.MyApplications.route) {
            MyApplicationsScreen(navController)
        }

    }
}


