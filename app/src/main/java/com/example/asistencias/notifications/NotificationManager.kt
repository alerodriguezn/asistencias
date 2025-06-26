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
    
    fun createComunicadoNotification(
        titulo: String, 
        descripcion: String, 
        targetRoles: List<String> = emptyList(),
        priority: String = "normal"
    ) {
        try {
            val currentUid = auth.currentUser?.uid ?: ""
            // Crear notificación en Firestore
            val notification = NotificationItem(
                id = "",
                title = "Nuevo Comunicado: $titulo",
                message = descripcion,
                read = false,
                timestamp = System.currentTimeMillis(),
                type = "comunicado",
                creatorUid = currentUid,
                readBy = emptyList(),
                targetRoles = targetRoles,
                priority = priority
            )
            
            // Guardar en Firestore
            db.collection("notifications")
                .add(notification)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Notificación creada con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al crear notificación en Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en createComunicadoNotification", e)
        }
    }
    
    // Notificación para cambio de estado en solicitudes de asistencia
    fun createAssistanceRequestStatusNotification(
        requestId: String,
        assistanceName: String,
        status: String,
        comments: String,
        studentId: String,
        studentName: String,
        reviewerName: String
    ) {
        try {
            val currentUid = auth.currentUser?.uid ?: ""
            val statusText = when (status) {
                "Aprobada" -> "aprobada"
                "Rechazada" -> "rechazada"
                else -> "actualizada"
            }
            
            val notification = NotificationItem(
                id = "",
                title = "Solicitud de Asistencia $statusText",
                message = "Tu solicitud para '$assistanceName' ha sido $statusText por $reviewerName. ${if (comments.isNotEmpty()) "Comentarios: $comments" else ""}",
                read = false,
                timestamp = System.currentTimeMillis(),
                type = "assistance_request_status",
                creatorUid = currentUid,
                readBy = emptyList(),
                targetRoles = emptyList(),
                priority = "normal",
                metadata = mapOf(
                    "requestId" to requestId,
                    "studentId" to studentId,
                    "status" to status,
                    "assistanceName" to assistanceName
                )
            )
            
            // Guardar en Firestore
            db.collection("notifications")
                .add(notification)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Notificación de cambio de estado creada con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al crear notificación de cambio de estado en Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en createAssistanceRequestStatusNotification", e)
        }
    }
    
    // Notificación para cambio de estado en aplicaciones de estudiantes
    fun createStudentApplicationStatusNotification(
        applicationId: String,
        assistanceName: String,
        status: String,
        comments: String,
        studentId: String,
        studentName: String,
        reviewerName: String
    ) {
        try {
            val currentUid = auth.currentUser?.uid ?: ""
            val statusText = when (status) {
                "Aprobada" -> "aprobada"
                "Rechazada" -> "rechazada"
                else -> "actualizada"
            }
            
            val notification = NotificationItem(
                id = "",
                title = "Aplicación de Asistencia $statusText",
                message = "Tu aplicación para '$assistanceName' ha sido $statusText por $reviewerName. ${if (comments.isNotEmpty()) "Comentarios: $comments" else ""}",
                read = false,
                timestamp = System.currentTimeMillis(),
                type = "student_application_status",
                creatorUid = currentUid,
                readBy = emptyList(),
                targetRoles = emptyList(),
                priority = "normal",
                metadata = mapOf(
                    "applicationId" to applicationId,
                    "studentId" to studentId,
                    "status" to status,
                    "assistanceName" to assistanceName
                )
            )
            
            // Guardar en Firestore
            db.collection("notifications")
                .add(notification)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Notificación de aplicación de estudiante creada con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al crear notificación de aplicación de estudiante en Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en createStudentApplicationStatusNotification", e)
        }
    }
    
    // Marcar como leída por el usuario actual
    fun markAsReadByUser(notificationId: String, userId: String? = null) {
        try {
            val currentUserId = userId ?: auth.currentUser?.uid
            if (currentUserId == null) {
                Log.e(TAG, "Usuario no autenticado, no se puede marcar notificación como leída")
                return
            }
            
            db.collection("notifications")
                .document(notificationId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val notification = document.toObject(NotificationItem::class.java)
                        notification?.let {
                            val updatedReadBy = it.readBy.toMutableList()
                            if (!updatedReadBy.contains(currentUserId)) {
                                updatedReadBy.add(currentUserId)
                            }
                            
                            // Actualizar tanto readBy como read (para compatibilidad)
                            val updates = mapOf(
                                "readBy" to updatedReadBy,
                                "read" to (updatedReadBy.isNotEmpty())
                            )
                            
                            db.collection("notifications")
                                .document(notificationId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Notificación marcada como leída por usuario: $notificationId")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error al marcar notificación como leída", e)
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error obteniendo notificación", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en markAsReadByUser", e)
        }
    }
    
    // Método legacy para compatibilidad
    fun markAsRead(notificationId: String) {
        markAsReadByUser(notificationId)
    }
    
    // Verificar si una notificación ha sido leída por el usuario actual
    fun isReadByUser(notification: NotificationItem, userId: String? = null): Boolean {
        val currentUserId = userId ?: auth.currentUser?.uid
        if (currentUserId == null) {
            return false
        }
        return notification.readBy.contains(currentUserId)
    }
    
    // Obtener notificaciones filtradas por usuario y rol
    fun getNotificationsForUser(
        userId: String? = null,
        userRole: String? = null,
        onSuccess: (List<NotificationItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val currentUserId = userId ?: auth.currentUser?.uid
            if (currentUserId == null) {
                onError(Exception("Usuario no autenticado"))
                return
            }
            
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
            Log.e(TAG, "Error en getNotificationsForUser", e)
            onError(e)
        }
    }
    
    // Marcar múltiples notificaciones como leídas
    fun markMultipleAsRead(notificationIds: List<String>, userId: String? = null) {
        val currentUserId = userId ?: auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "Usuario no autenticado, no se pueden marcar notificaciones como leídas")
            return
        }
        
        notificationIds.forEach { notificationId ->
            markAsReadByUser(notificationId, currentUserId)
        }
    }
    
    // Eliminar notificaciones expiradas
    fun cleanupExpiredNotifications() {
        try {
            val currentTime = System.currentTimeMillis()
            
            db.collection("notifications")
                .whereLessThan("expiresAt", currentTime)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        deleteNotification(document.id)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error limpiando notificaciones expiradas", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en cleanupExpiredNotifications", e)
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