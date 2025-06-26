package com.example.asistencias.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.asistencias.core.navigation.Routes
import com.example.asistencias.core.navigation.navigateIntelligently
import com.example.asistencias.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class HomeMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar datos del usuario actual
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { firebaseUser ->
            db.collection("usuarios").document(firebaseUser.uid).get()
                .addOnSuccessListener { document ->
                    currentUser = document.toObject(User::class.java)
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Mensaje de bienvenida
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DFEB)),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "¡Bienvenido, ${currentUser?.nombre ?: "Usuario"}!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sistema de Gestión de Asistencias",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    if (currentUser?.rol != null) {
                        Text(
                            text = "Rol: ${currentUser!!.rol}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menú de opciones según el rol
            val menuItems = if (currentUser?.rol == "Administrador" || 
                               auth.currentUser?.email in listOf("nesa14@estudiantec.cr")) {
                // Menú para administradores
                listOf(
                    HomeMenuItem(
                        title = "Gestión de Aplicaciones",
                        description = "Revisar y aprobar aplicaciones de estudiantes",
                        icon = Icons.Default.Person,
                        route = Routes.StudentApplicationsManagement.route,
                        color = Color(0xFF4CAF50)
                    ),
                    HomeMenuItem(
                        title = "Solicitudes de Asistencia",
                        description = "Gestionar solicitudes de asistencia",
                        icon = Icons.Default.List,
                        route = Routes.AssistanceRequests.route,
                        color = Color(0xFF2196F3)
                    ),
                    HomeMenuItem(
                        title = "Tipos de Asistencia",
                        description = "Administrar tipos de asistencia disponibles",
                        icon = Icons.Default.Category,
                        route = Routes.AssistanceTypes.route,
                        color = Color(0xFFFF9800)
                    ),
                    HomeMenuItem(
                        title = "Cursos",
                        description = "Gestionar cursos del sistema",
                        icon = Icons.Default.School,
                        route = Routes.Courses.route,
                        color = Color(0xFF9C27B0)
                    ),
                    HomeMenuItem(
                        title = "Jornadas",
                        description = "Administrar jornadas académicas",
                        icon = Icons.Default.CalendarToday,
                        route = Routes.Jornadas.route,
                        color = Color(0xFF607D8B)
                    ),
                    HomeMenuItem(
                        title = "Gestión de Usuarios",
                        description = "Administrar usuarios del sistema",
                        icon = Icons.Default.People,
                        route = Routes.UserManagement.route,
                        color = Color(0xFF795548)
                    )
                )
            } else {
                // Menú para estudiantes
                listOf(
                    HomeMenuItem(
                        title = "Asistencias Disponibles",
                        description = "Ver y aplicar a asistencias disponibles",
                        icon = Icons.Default.Search,
                        route = Routes.AvailableAssistances.route,
                        color = Color(0xFF4CAF50)
                    ),
                    HomeMenuItem(
                        title = "Mis Aplicaciones",
                        description = "Ver el estado de mis aplicaciones",
                        icon = Icons.Default.Person,
                        route = Routes.MyApplications.route,
                        color = Color(0xFF9C27B0)
                    ),
                    HomeMenuItem(
                        title = "Notificaciones",
                        description = "Ver notificaciones del sistema",
                        icon = Icons.Default.Notifications,
                        route = Routes.Notifications.route,
                        color = Color(0xFF2196F3)
                    ),
                    HomeMenuItem(
                        title = "Comunicados",
                        description = "Ver comunicados activos",
                        icon = Icons.Default.Info,
                        route = Routes.ComunicadosInicio.route,
                        color = Color(0xFFFF9800)
                    )
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuItems) { menuItem ->
                    HomeMenuCard(
                        menuItem = menuItem,
                        onClick = {
                            // Usar la función de navegación inteligente
                            navController.navigateIntelligently(menuItem.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeMenuCard(
    menuItem: HomeMenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = menuItem.color.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = menuItem.icon,
                contentDescription = menuItem.title,
                tint = menuItem.color,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = menuItem.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = menuItem.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 