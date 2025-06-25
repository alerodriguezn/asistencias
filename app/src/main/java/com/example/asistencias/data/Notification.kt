package com.example.asistencias.data

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val read: Boolean = false,
    val timestamp: Long = 0L,
    val type: String = "general", // "comunicado", "general", etc.
    val creatorUid: String = "",
    val readBy: List<String> = emptyList(),
    val targetRoles: List<String> = emptyList(),
    val priority: String = "normal",
    val expiresAt: Long? = null,
    val actionUrl: String? = null,
    val metadata: Map<String, Any> = emptyMap()
) 