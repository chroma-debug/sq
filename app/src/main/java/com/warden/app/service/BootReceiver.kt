package com.warden.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.warden.app.data.repository.WardenPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = WardenPreferences(context)
            if (prefs.isSessionActive) {
                WardenForegroundService.start(context)
            }
        }
    }
}
