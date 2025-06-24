package com.example.asistencias.notifications

import android.content.Context

object NotificationLocalStore {
    private const val PREFS_NAME = "shown_notifications"
    private const val KEY_IDS = "ids"
    private const val KEY_LAST_TIMESTAMP = "last_timestamp"

    fun getShownNotificationIds(context: Context): MutableSet<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_IDS, mutableSetOf()) ?: mutableSetOf()
    }

    fun addShownNotificationId(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ids = getShownNotificationIds(context)
        ids.add(id)
        prefs.edit().putStringSet(KEY_IDS, ids).apply()
    }

    fun getLastNotificationTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
    }

    fun setLastNotificationTimestamp(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_TIMESTAMP, timestamp).apply()
    }
} 