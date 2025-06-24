package com.example.asistencias.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asistencias.data.NotificationItem
import com.example.asistencias.notifications.NotificationManager
import com.example.asistencias.notifications.NotificationSyncService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class NotificationViewModel : ViewModel() {
    
    private val TAG = "NotificationViewModel"
    private var notificationManager: NotificationManager? = null
    private var notificationSyncService: NotificationSyncService? = null
    
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    
    fun initialize(context: Context) {
        notificationManager = NotificationManager(context)
        notificationSyncService = NotificationSyncService(context)
        
        // Iniciar sincronización en tiempo real
        notificationSyncService?.startSync(this)
        
        // Cargar notificaciones iniciales
        loadNotifications()
    }
    
    fun loadNotifications(userRole: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            if (currentUserId == null) {
                _error.value = "Usuario no autenticado"
                _isLoading.value = false
                return@launch
            }
            
            notificationManager?.getNotificationsForUser(
                userId = currentUserId,
                userRole = userRole,
                onSuccess = { notifications ->
                    _notifications.value = notifications
                    updateUnreadCount(notifications)
                    _isLoading.value = false
                },
                onError = { exception ->
                    _error.value = "Error cargando notificaciones: ${exception.message}"
                    _isLoading.value = false
                    Log.e(TAG, "Error loading notifications", exception)
                }
            )
        }
    }
    
    // Método llamado por el servicio de sincronización
    fun updateNotificationsFromSync(notifications: List<NotificationItem>) {
        _notifications.value = notifications
        updateUnreadCount(notifications)
    }
    
    private fun updateUnreadCount(notifications: List<NotificationItem>) {
        if (currentUserId == null) {
            _unreadCount.value = 0
            return
        }
        
        val unreadCount = notifications.count { notification ->
            val notificationManager = notificationManager
            if (notificationManager == null) {
                false
            } else {
                !notificationManager.isReadByUser(notification, currentUserId)
            }
        }
        _unreadCount.value = unreadCount
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            if (currentUserId == null) {
                _error.value = "Usuario no autenticado"
                return@launch
            }
            
            notificationManager?.markAsReadByUser(notificationId, currentUserId)
            
            // Actualizar la lista local
            val updatedNotifications = _notifications.value.map { notification ->
                if (notification.id == notificationId) {
                    val updatedReadBy = notification.readBy.toMutableList()
                    currentUserId?.let { uid ->
                        if (!updatedReadBy.contains(uid)) {
                            updatedReadBy.add(uid)
                        }
                    }
                    notification.copy(readBy = updatedReadBy, read = updatedReadBy.isNotEmpty())
                } else {
                    notification
                }
            }
            _notifications.value = updatedNotifications
            updateUnreadCount(updatedNotifications)
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            if (currentUserId == null) {
                _error.value = "Usuario no autenticado"
                return@launch
            }
            
            val unreadNotifications = _notifications.value.filter { notification ->
                val notificationManager = notificationManager
                if (notificationManager == null) {
                    false
                } else {
                    !notificationManager.isReadByUser(notification, currentUserId)
                }
            }
            
            val notificationIds = unreadNotifications.map { it.id }
            notificationManager?.markMultipleAsRead(notificationIds, currentUserId)
            
            // Actualizar la lista local
            val updatedNotifications = _notifications.value.map { notification ->
                val notificationManager = notificationManager
                if (notificationManager != null && !notificationManager.isReadByUser(notification, currentUserId)) {
                    val updatedReadBy = notification.readBy.toMutableList()
                    currentUserId?.let { uid ->
                        if (!updatedReadBy.contains(uid)) {
                            updatedReadBy.add(uid)
                        }
                    }
                    notification.copy(readBy = updatedReadBy, read = updatedReadBy.isNotEmpty())
                } else {
                    notification
                }
            }
            _notifications.value = updatedNotifications
            updateUnreadCount(updatedNotifications)
        }
    }
    
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationManager?.deleteNotification(notificationId)
            
            // Actualizar la lista local
            val updatedNotifications = _notifications.value.filter { it.id != notificationId }
            _notifications.value = updatedNotifications
            updateUnreadCount(updatedNotifications)
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun isReadByCurrentUser(notification: NotificationItem): Boolean {
        if (currentUserId == null) {
            return false
        }
        val notificationManager = notificationManager
        return if (notificationManager == null) {
            false
        } else {
            notificationManager.isReadByUser(notification, currentUserId)
        }
    }
    
    fun getNotificationsByPriority(priority: String): List<NotificationItem> {
        return _notifications.value.filter { it.priority == priority }
    }
    
    fun getUnreadNotifications(): List<NotificationItem> {
        if (currentUserId == null) {
            return emptyList()
        }
        return _notifications.value.filter { notification ->
            val notificationManager = notificationManager
            if (notificationManager == null) {
                false
            } else {
                !notificationManager.isReadByUser(notification, currentUserId)
            }
        }
    }
    
    fun getReadNotifications(): List<NotificationItem> {
        if (currentUserId == null) {
            return emptyList()
        }
        return _notifications.value.filter { notification ->
            val notificationManager = notificationManager
            if (notificationManager == null) {
                false
            } else {
                notificationManager.isReadByUser(notification, currentUserId)
            }
        }
    }
    
    fun refreshNotifications(userRole: String? = null) {
        loadNotifications(userRole)
    }
    
    // Limpiar notificaciones antiguas
    fun cleanupOldNotifications() {
        notificationSyncService?.cleanupOldNotifications()
    }
    
    // Método para manejar notificaciones marcadas como leídas desde push
    fun handleNotificationMarkedFromPush(notificationId: String) {
        viewModelScope.launch {
            // Actualizar la lista local inmediatamente
            val updatedNotifications = _notifications.value.map { notification ->
                if (notification.id == notificationId) {
                    val updatedReadBy = notification.readBy.toMutableList()
                    currentUserId?.let { uid ->
                        if (!updatedReadBy.contains(uid)) {
                            updatedReadBy.add(uid)
                        }
                    }
                    notification.copy(readBy = updatedReadBy, read = updatedReadBy.isNotEmpty())
                } else {
                    notification
                }
            }
            _notifications.value = updatedNotifications
            updateUnreadCount(updatedNotifications)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Detener la sincronización cuando se destruye el ViewModel
        notificationSyncService?.stopSync()
    }
} 