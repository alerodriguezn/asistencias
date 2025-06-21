package com.example.asistencias.data

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val read: Boolean = false,
    val timestamp: Long = 0L,
    val type: String = "general" // "comunicado", "general", etc.
) 