package com.example.asistencias.notifications

import android.content.Context
import android.util.Log
import com.example.asistencias.data.NotificationItem
import com.example.asistencias.screens.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NotificationSyncService(private val context: Context) {
    
    private val TAG = "NotificationSyncService"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var notificationViewModel: NotificationViewModel? = null
    
    fun startSync(notificationViewModel: NotificationViewModel) {
        this.notificationViewModel = notificationViewModel
        setupRealtimeListener()
    }
    
    fun stopSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
        notificationViewModel = null
    }
    
    private fun setupRealtimeListener() {
        try {
            listenerRegistration = db.collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        Log.e(TAG, "Error listening to notifications: ", exception)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val notifications = mutableListOf<NotificationItem>()
                        
                        for (document in snapshots) {
                            try {
                                val notification = document.toObject(NotificationItem::class.java)
                                notification?.let {
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
                        
                        // Actualizar el ViewModel con las nuevas notificaciones
                        notificationViewModel?.let { viewModel ->
                            // Solo actualizar si hay cambios reales
                            val currentNotifications = viewModel.notifications.value
                            if (notifications != currentNotifications) {
                                viewModel.updateNotificationsFromSync(notifications)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up realtime listener", e)
        }
    }
    
    // Método para obtener notificaciones filtradas por rol
    fun getNotificationsForRole(
        userRole: String?,
        onSuccess: (List<NotificationItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            db.collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val notifications = mutableListOf<NotificationItem>()
                    
                    for (document in documents) {
                        try {
                            val notification = document.toObject(NotificationItem::class.java)
                            notification?.let {
                                val notificationWithId = if (it.id.isEmpty()) {
                                    it.copy(id = document.id)
                                } else {
                                    it
                                }
                                
                                // Filtrar por rol si se especifica
                                if (userRole == null || 
                                    notificationWithId.targetRoles.isEmpty() || 
                                    notificationWithId.targetRoles.contains(userRole)) {
                                    notifications.add(notificationWithId)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing notification document: ${document.id}", e)
                        }
                    }
                    
                    onSuccess(notifications)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error obteniendo notificaciones", e)
                    onError(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getNotificationsForRole", e)
            onError(e)
        }
    }
    
    // Método para limpiar notificaciones antiguas
    fun cleanupOldNotifications(maxAgeDays: Int = 30) {
        try {
            val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
            
            db.collection("notifications")
                .whereLessThan("timestamp", cutoffTime)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Solo eliminar si no tiene lectores o si todos los lectores son antiguos
                        val notification = document.toObject(NotificationItem::class.java)
                        if (notification?.readBy?.isEmpty() == true) {
                            db.collection("notifications")
                                .document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Notificación antigua eliminada: ${document.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error eliminando notificación antigua: ${document.id}", e)
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error limpiando notificaciones antiguas", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en cleanupOldNotifications", e)
        }
    }
} 