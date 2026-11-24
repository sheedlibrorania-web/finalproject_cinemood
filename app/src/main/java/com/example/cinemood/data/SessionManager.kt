package com.example.cinemood.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cinemood_session", Context.MODE_PRIVATE)
    val userId: Long get() = prefs.getLong("user_id", -1L)
    val username: String get() = prefs.getString("username", "").orEmpty()
    fun save(userId: Long, username: String) {
        prefs.edit().putLong("user_id", userId).putString("username", username).apply()
    }
    fun logout() { prefs.edit().clear().apply() }
}
