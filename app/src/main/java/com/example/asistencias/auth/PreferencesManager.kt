package com.example.asistencias.auth

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun setRememberMeState(value: Boolean) {
        prefs.edit().putBoolean("remember_me", value).apply()
    }

    fun getRememberMeState(): Boolean {
        return prefs.getBoolean("remember_me", false)
    }
}
