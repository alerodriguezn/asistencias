package com.example.asistencias.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun fetchUsersFromFirestore(): List<User> {
    val db = FirebaseFirestore.getInstance()
    val users = mutableListOf<User>()

    try {
        val snapshot = db.collection("usuarios").get().await()
        for (document in snapshot.documents) {
            val user = document.toObject(User::class.java)
            if (user != null) {
                users.add(user)
            }
        }
        Log.d("Firestore", "Usuarios obtenidos: ${users.size}")
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("Firestore", "Error al obtener usuarios: ${e.message}")
    }

    return users
}


@Composable
fun UserManagementScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Nombre") }
    val filters = listOf("Nombre", "Cédula", "Correo", "Carnet", "Carrera")

    var showEditRoleDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }


    // Obtener los usuarios desde Firestore
    var users by remember { mutableStateOf(emptyList<User>()) }

    LaunchedEffect(Unit) {
        users = fetchUsersFromFirestore()
    }

    // Filtrar usuarios según la búsqueda
    val filteredUsers = users.filter { user ->
        when (selectedFilter) {
            "Nombre" -> user.nombre.contains(searchQuery, ignoreCase = true)
            "Cédula" -> user.cedula.contains(searchQuery, ignoreCase = true)
            "Correo" -> user.correo.contains(searchQuery, ignoreCase = true)
            "Carnet" -> user.carnet.contains(searchQuery, ignoreCase = true)
            "Carrera" -> user.carrera.contains(searchQuery, ignoreCase = true)
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado con curva
        HeaderWithCurve()
        // Barra de búsqueda y filtro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f).height(60.dp),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar usuario") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(28.dp)
            )

            // Menú desplegable para filtros
            DropdownMenuFilter(
                selectedFilter = selectedFilter,
                filters = filters,
                onFilterSelected = { selectedFilter = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de usuarios
        LazyColumn {
            items(filteredUsers) { user ->
                UserCard(
                    user = user,
                    onEditRoleClick = {
                        // Lógica para abrir el diálogo de cambiar rol
                        selectedUser = user
                        showEditRoleDialog = true
                    },
                    onToggleActiveClick = {
                        // Lógica para activar/desactivar usuario
                        Log.d("UserManagement", "Activar/Desactivar usuario: ${user.nombre}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showEditRoleDialog && selectedUser != null) {
        EditRole(
            user = selectedUser!!,
            onDismiss = { showEditRoleDialog = false },
            onRoleChange = { newRole ->
                // Actualiza el rol en Firestore y en la lista de usuarios
                updateUserRoleInFirestore(selectedUser!!.cedula, newRole) { success ->
                    if (success) {
                        users = users.map { user ->
                            if (user.cedula == selectedUser!!.cedula) {
                                user.copy(rol = newRole)
                            } else user
                        }
                    }
                }
                showEditRoleDialog = false
            }
        )
    }
}

@Composable
fun HeaderWithCurve() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, height * 0.7f)
                cubicTo(
                    width * 0.25f, height * 1f,
                    width * 0.75f, height * 1f,
                    width, height * 0.7f
                )
                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }

            drawPath(
                path = path,
                color = primaryColor
            )
        }

        Text(
            text = "Gestión de Usuarios",
            style = MaterialTheme.typography.headlineLarge,
            color = onPrimaryColor,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable
fun DropdownMenuFilter(
    selectedFilter: String,
    filters: List<String>,
    onFilterSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true },
            modifier = Modifier.height(50.dp)) {
            Text(selectedFilter)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter) },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun UserCard(user: User, onEditRoleClick: () -> Unit, onToggleActiveClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) } // Controla la expansión de la tarjeta
    var menuExpanded by remember { mutableStateOf(false) } // Controla el menú desplegable

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }, // Alternar expansión al hacer clic en la tarjeta
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Alinear contenido y menú
        ) {
            // Información principal del usuario
            Column {
                Text(
                    text = "${user.nombre} ${user.apellido1} ${user.apellido2}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = if (user.rol.isNotEmpty()) user.rol else "Desconocido")
            }

            // Botón de opciones (tres puntos) y menú desplegable
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cambiar Rol") },
                        onClick = {
                            menuExpanded = false
                            onEditRoleClick() // Llama a la función para cambiar el rol
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Activar/Desactivar") },
                        onClick = {
                            menuExpanded = false
                            onToggleActiveClick() // Llama a la función para activar/desactivar
                        }
                    )
                }
            }
        }

        // Mostrar datos adicionales si la tarjeta está expandida
        if (expanded) {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Cédula: ${user.cedula}")
                Text(text = "Correo: ${user.correo}")
                Text(text = "Carnet: ${user.carnet}")
                Text(text = "Carrera: ${user.carrera}")
            }
        }
    }
}

fun updateUserRoleInFirestore(cedula: String, newRole: String, onComplete: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("usuarios")
        .whereEqualTo("cedula", cedula)
        .get()
        .addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val document = snapshot.documents[0]
                document.reference.update("rol", newRole)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            } else {
                onComplete(false)
            }
        }
        .addOnFailureListener { onComplete(false) }
}

// Data class para representar un usuario
data class User(
    val nombre: String = "",
    val apellido1: String = "",
    val apellido2: String = "",
    val cedula: String = "",
    val correo: String = "",
    val carnet: String = "",
    val carrera: String = "",
    val rol: String = ""
)

@Preview(showBackground = true)
@Composable
fun PreviewUserManagementScreen() {
    MaterialTheme {
        UserManagementScreen()
    }
}