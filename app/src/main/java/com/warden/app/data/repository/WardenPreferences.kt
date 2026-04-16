package com.warden.app.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * General (non-sensitive) app preferences for session state, break mode, etc.
 */
class WardenPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("warden_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SESSION_ACTIVE = "session_active"
        private const val KEY_BREAK_MODE = "break_mode"
        private const val KEY_BREAK_UNTIL = "break_until"
        private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
        private const val KEY_LAST_LOCKED_PACKAGE = "last_locked_package"
    }

    var isSessionActive: Boolean
        get() = prefs.getBoolean(KEY_SESSION_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_SESSION_ACTIVE, value).apply()

    var isBreakMode: Boolean
        get() = prefs.getBoolean(KEY_BREAK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_BREAK_MODE, value).apply()

    var breakUntilMillis: Long
        get() = prefs.getLong(KEY_BREAK_UNTIL, 0L)
        set(value) = prefs.edit().putLong(KEY_BREAK_UNTIL, value).apply()

    var isScheduleEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, value).apply()

    var lastLockedPackage: String
        get() = prefs.getString(KEY_LAST_LOCKED_PACKAGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_LOCKED_PACKAGE, value).apply()

    fun startBreak(durationMinutes: Int) {
        isBreakMode = true
        breakUntilMillis = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
    }

    fun isBreakActive(): Boolean {
        if (!isBreakMode) return false
        if (System.currentTimeMillis() > breakUntilMillis) {
            isBreakMode = false
            return false
        }
        return true
    }

    fun remainingBreakSeconds(): Long {
        if (!isBreakActive()) return 0
        return (breakUntilMillis - System.currentTimeMillis()) / 1000
    }
}
