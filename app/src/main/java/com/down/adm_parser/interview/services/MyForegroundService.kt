package com.down.adm_parser.interview.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.down.adm_parser.R

class MyForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(102, createNotification())
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val channelId = "foreground_service_channel"
        val channelName = "Foreground Service Channel"

        // Create NotificationChannel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Build the notification
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Music Playing")
            .setContentText("Your music is playing in the background.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

}