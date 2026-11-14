package com.example.util

import android.content.Context
import android.content.SharedPreferences

class SecurityPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("security_settings", Context.MODE_PRIVATE)

    var isPinEnabled: Boolean
        get() = prefs.getBoolean("is_pin_enabled", false)
        set(value) = prefs.edit().putBoolean("is_pin_enabled", value).apply()

    var pinCode: String
        get() = prefs.getString("pin_code", "") ?: ""
        set(value) = prefs.edit().putString("pin_code", value).apply()

    var userEmail: String
        get() = prefs.getString("user_email", "usuario@email.com") ?: "usuario@email.com"
        set(value) = prefs.edit().putString("user_email", value).apply()

    var isAutoEmailEnabled: Boolean
        get() = prefs.getBoolean("is_auto_email_enabled", true)
        set(value) = prefs.edit().putBoolean("is_auto_email_enabled", value).apply()

    var isNotificationCaptureEnabled: Boolean
        get() = prefs.getBoolean("is_notification_capture_enabled", true)
        set(value) = prefs.edit().putBoolean("is_notification_capture_enabled", value).apply()

    fun verifyPin(input: String): Boolean {
        return !isPinEnabled || pinCode == input
    }
}
