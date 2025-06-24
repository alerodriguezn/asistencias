package com.example.asistencias.notifications

import android.util.Log

object NotificationDisplayControl {
    @Volatile
    var notificationsScreenActive: Boolean = false
    
    fun isNotificationsScreenActive(): Boolean {
        Log.d("NotificationDisplayControl", "Estado de pantalla de notificaciones: $notificationsScreenActive")
        return notificationsScreenActive
    }
} 