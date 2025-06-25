package com.example.asistencias.notifications

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.asistencias.data.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GlobalNotificationListener() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val notificationService = NotificationService(context)
    val TAG = "GlobalNotificationListener"
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        db.collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening to notifications: ", exception)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val shownIds = NotificationLocalStore.getShownNotificationIds(context)
                    val lastTimestamp = NotificationLocalStore.getLastNotificationTimestamp(context)
                    var maxTimestamp = lastTimestamp
                    
                    // Verificar si la pantalla de notificaciones está activa
                    val isNotificationsScreenActive = NotificationDisplayControl.isNotificationsScreenActive()
                    
                    for (document in snapshots) {
                        try {
                            val notification = document.toObject(NotificationItem::class.java)
                            notification?.let {
                                val notificationWithId = if (it.id.isEmpty()) {
                                    it.copy(id = document.id)
                                } else {
                                    it
                                }
                                
                                // Solo mostrar si es realmente nueva y la pantalla de notificaciones NO está activa
                                if (!notificationWithId.read &&
                                    notificationWithId.creatorUid != currentUid &&
                                    !shownIds.contains(notificationWithId.id) &&
                                    notificationWithId.timestamp > lastTimestamp &&
                                    !isNotificationsScreenActive
                                ) {
                                    Log.d(TAG, "Mostrando notificación push: ${notificationWithId.title}")
                                    notificationService.showComunicadoNotification(
                                        notificationWithId.title,
                                        notificationWithId.message,
                                        notificationWithId.id
                                    )
                                    NotificationLocalStore.addShownNotificationId(context, notificationWithId.id)
                                } else if (isNotificationsScreenActive) {
                                    Log.d(TAG, "No mostrando notificación push - pantalla de notificaciones activa")
                                }
                                
                                if (notificationWithId.timestamp > maxTimestamp) {
                                    maxTimestamp = notificationWithId.timestamp
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing notification document: ${document.id}", e)
                        }
                    }
                    // Actualizar el último timestamp revisado
                    NotificationLocalStore.setLastNotificationTimestamp(context, maxTimestamp)
                }
            }
    }
} 