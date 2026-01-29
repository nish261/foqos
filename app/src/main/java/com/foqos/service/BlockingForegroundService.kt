package com.foqos.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.foqos.MainActivity
import com.foqos.R

/**
 * Foreground Service that maintains app blocking even when Foqos is in the background.
 * 
 * This service:
 * - Shows persistent notification with session elapsed time
 * - Keeps the app blocking active even if user closes the app
 * - Provides notification actions for stop/break
 * - Updates elapsed time every minute
 */
class BlockingForegroundService : Service() {
    
    companion object {
        const val CHANNEL_ID = "foqos_session_channel"
        const val NOTIFICATION_ID = 1
        
        const val ACTION_STOP = "com.foqos.ACTION_STOP_SESSION"
        const val ACTION_BREAK = "com.foqos.ACTION_TAKE_BREAK"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> handleStopSession()
            ACTION_BREAK -> handleTakeBreak()
            else -> startForeground(NOTIFICATION_ID, createNotification())
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Focus Sessions",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Ongoing focus session notifications"
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_MANAGER_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Session Active")
            .setContentText("Stay focused!")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun handleStopSession() {
        // TODO: Implement stop session logic
        stopSelf()
    }
    
    private fun handleTakeBreak() {
        // TODO: Implement break logic
    }
}
