package com.warden.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.warden.app.data.repository.WardenPreferences
import com.warden.app.data.repository.WardenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_START_SESSION = "com.warden.app.START_SCHEDULED_SESSION"
        const val ACTION_STOP_SESSION = "com.warden.app.STOP_SCHEDULED_SESSION"
        const val ACTION_START_BREAK = "com.warden.app.START_BREAK"
        const val ACTION_END_BREAK = "com.warden.app.END_BREAK"
        const val EXTRA_BREAK_DURATION = "break_duration_minutes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = WardenPreferences(context)

        when (intent.action) {
            ACTION_START_SESSION -> {
                WardenForegroundService.start(context)
            }
            ACTION_STOP_SESSION -> {
                WardenForegroundService.stop(context)
            }
            ACTION_START_BREAK -> {
                val duration = intent.getIntExtra(EXTRA_BREAK_DURATION, 10)
                prefs.startBreak(duration)
            }
            ACTION_END_BREAK -> {
                prefs.isBreakMode = false
            }
        }
    }
}
