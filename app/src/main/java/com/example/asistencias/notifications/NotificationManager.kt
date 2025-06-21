package com.example.asistencias.notifications

import android.content.Context
import android.util.Log
import com.example.asistencias.data.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NotificationManager(private val context: Context) {
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationService = NotificationService(context)
    private val TAG = "NotificationManager"
    
    fun createComunicadoNotification(titulo: String, descripcion: String) {
        try {
            // Crear notificación en Firestore
            val notification = NotificationItem(
                id = "",
                title = "Nuevo Comunicado: $titulo",
                message = descripcion,
                read = false,
                timestamp = System.currentTimeMillis(),
                type = "comunicado"
            )
            
            // Guardar en Firestore
            db.collection("notifications")
                .add(notification)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Notificación creada con ID: ${documentReference.id}")
                    
                    // Mostrar notificación en la barra de tareas
                    notificationService.showComunicadoNotification(titulo, descripcion)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al crear notificación en Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en createComunicadoNotification", e)
        }
    }
    
    fun markAsRead(notificationId: String) {
        try {
            db.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .addOnSuccessListener {
                    Log.d(TAG, "Notificación marcada como leída: $notificationId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al marcar notificación como leída", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en markAsRead", e)
        }
    }
    
    fun deleteNotification(notificationId: String) {
        try {
            db.collection("notifications")
                .document(notificationId)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Notificación eliminada: $notificationId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al eliminar notificación", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en deleteNotification", e)
        }
    }
    
    // Función para migrar notificaciones existentes que no tienen el campo type
    fun migrateExistingNotifications() {
        try {
            db.collection("notifications")
                .whereEqualTo("type", "")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Asumir que las notificaciones existentes son de comunicados
                        db.collection("notifications")
                            .document(document.id)
                            .update("type", "comunicado")
                            .addOnSuccessListener {
                                Log.d(TAG, "Notificación migrada: ${document.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error migrando notificación: ${document.id}", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error obteniendo notificaciones para migrar", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en migrateExistingNotifications", e)
        }
    }
} 