package com.example.SagyouNippou

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isTracking = false
    private var startTimeMillis = 0L
    private var currentClassificationName = "None"

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_CLASSIFICATION_NAME = "EXTRA_CLASSIFICATION_NAME"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val classification = intent.getStringExtra(EXTRA_CLASSIFICATION_NAME) ?: "Tracking"
                startTracking(classification)
            }
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking(classificationName: String) {
        if (isTracking && currentClassificationName == classificationName) return

        currentClassificationName = classificationName
        if (!isTracking) {
            startTimeMillis = System.currentTimeMillis()
            isTracking = true
        }

        // Display the foreground notification to prevent Android from killing the app
        startForeground(NOTIFICATION_ID, buildNotification("00:00:00"))

        // Start an internal background ticker loop to update the notification text
        serviceScope.launch {
            while (isTracking) {
                delay(1000)
                val elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
                val formattedTime = formatElapsedTime(elapsedSeconds)
                updateNotification(formattedTime)
            }
        }
    }

    private fun stopTracking() {
        isTracking = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking: $currentClassificationName")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play) // Standard native Android icon placeholder
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        // Note: SonarQube might complain later if a real icon vector isn't assigned,
        // but this native placeholder keeps us moving for now.
    }

    private fun updateNotification(updatedTime: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(updatedTime))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Time Tracker Notifications",
                NotificationManager.IMPORTANCE_LOW // Low importance prevents annoying sounds every second
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatElapsedTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}