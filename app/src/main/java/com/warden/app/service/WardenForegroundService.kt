package com.warden.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.warden.app.R
import com.warden.app.WardenApplication
import com.warden.app.data.repository.WardenPreferences
import com.warden.app.ui.main.MainActivity

class WardenForegroundService : Service() {

    companion object {
        const val ACTION_START = "com.warden.app.START"
        const val ACTION_STOP = "com.warden.app.STOP"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, WardenForegroundService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, WardenForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var prefs: WardenPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = WardenPreferences(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                prefs.isSessionActive = true
                startForeground(NOTIFICATION_ID, buildNotification())
            }
            ACTION_STOP -> {
                prefs.isSessionActive = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, WardenForegroundService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, WardenApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("THE WARDEN IS ACTIVE")
            .setContentText("FOCUS SESSION IN PROGRESS. STAY ON TASK.")
            .setSmallIcon(R.drawable.ic_warden)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_warden, "END SESSION", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
