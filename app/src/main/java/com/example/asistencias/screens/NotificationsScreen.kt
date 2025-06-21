package com.example.asistencias.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.asistencias.core.navigation.Routes
import com.example.asistencias.data.NotificationItem
import com.example.asistencias.notifications.NotificationManager
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController? = null) {
    val notifications = remember { mutableStateListOf<NotificationItem>() }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val notificationManager = remember { NotificationManager(context) }
    val TAG = "NotificationsScreen"
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var notificationToDelete by remember { mutableStateOf<NotificationItem?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Migrar notificaciones existentes que no tienen el campo type
            notificationManager.migrateExistingNotifications()
            
            db.collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        Log.e(TAG, "Error listening to notifications: ", exception)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        notifications.clear()
                        for (document in snapshots) {
                            try {
                                val notification = document.toObject(NotificationItem::class.java)
                                notification?.let {
                                    // Asegurar que el ID esté presente
                                    val notificationWithId = if (it.id.isEmpty()) {
                                        it.copy(id = document.id)
                                    } else {
                                        it
                                    }
                                    notifications.add(notificationWithId)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing notification document: ${document.id}", e)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up notifications listener", e)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = Icons.Default.Notifications,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Notifications"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (notifications.isEmpty()) {
                EmptyNotificationsState()
            } else {
                NotificationsList(
                    notifications = notifications,
                    onNotificationClick = { notification ->
                        try {
                            // Marcar como leída si no está leída
                            if (!notification.read && notification.id.isNotEmpty()) {
                                notificationManager.markAsRead(notification.id)
                            }
                            
                            // Navegar según el tipo de notificación
                            when (notification.type) {
                                "comunicado" -> {
                                    navController?.navigate(Routes.ComunicadosInicio.route)
                                }
                                else -> {
                                    // Para otros tipos de notificaciones, quedarse en la pantalla actual
                                    Log.d(TAG, "Notificación de tipo: ${notification.type}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error handling notification click", e)
                        }
                    },
                    onDeleteNotification = { notification ->
                        notificationToDelete = notification
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && notificationToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                notificationToDelete = null
            },
            title = { Text("Eliminar Notificación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta notificación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificationToDelete?.let { notification ->
                            notificationManager.deleteNotification(notification.id)
                        }
                        showDeleteDialog = false
                        notificationToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        notificationToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "No notifications",
            modifier = Modifier
                .width(64.dp)
                .height(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay notificaciones",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cuando recibas notificaciones, aparecerán aquí",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NotificationsList(
    notifications: List<NotificationItem>,
    onNotificationClick: (NotificationItem) -> Unit,
    onDeleteNotification: (NotificationItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notifications) { notification ->
            NotificationCard(
                notification = notification,
                onClick = { onNotificationClick(notification) },
                onDelete = { onDeleteNotification(notification) }
            )
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    if (notification.read) {
        // Estilo para notificaciones leídas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 1.dp,
                focusedElevation = 1.dp,
                hoveredElevation = 1.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Notification status",
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title.ifEmpty { "Sin título" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.message.ifEmpty { "Sin mensaje" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val dateText = try {
                        if (notification.timestamp > 0) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notification.timestamp))
                        } else {
                            "Fecha no disponible"
                        }
                    } catch (e: Exception) {
                        "Fecha no disponible"
                    }
                    
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar notificación",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    } else {
        // Estilo completamente diferente para notificaciones no leídas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp,
                focusedElevation = 4.dp,
                hoveredElevation = 5.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icono más grande y llamativo
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "New notification",
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title.ifEmpty { "Sin título" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = notification.message.ifEmpty { "Sin mensaje" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val dateText = try {
                        if (notification.timestamp > 0) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notification.timestamp))
                        } else {
                            "Fecha no disponible"
                        }
                    } catch (e: Exception) {
                        "Fecha no disponible"
                    }
                    
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botón de eliminar con estilo diferente
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar notificación",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}