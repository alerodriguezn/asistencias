package com.example.asistencias.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.asistencias.MainActivity
import com.example.asistencias.R
import com.example.asistencias.core.navigation.Routes

class NotificationService(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "comunicados_channel"
        const val CHANNEL_NAME = "Comunicados"
        const val CHANNEL_DESCRIPTION = "Notificaciones de comunicados"
        const val NOTIFICATION_ID = 1
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showComunicadoNotification(titulo: String, descripcion: String) {
        // Intent para abrir la app en la pantalla de comunicados
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", Routes.ComunicadosInicio.route)
            putExtra("notification_type", "comunicado")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Nuevo Comunicado: $titulo")
            .setContentText(descripcion)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
} 