package com.foqos.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.foqos.MainActivity
import com.foqos.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for all app notifications.
 * Creates notification channels and builds notifications for sessions.
 */
@Singleton
class FoqosNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val CHANNEL_SESSION = "foqos_session_channel"
        const val CHANNEL_REMINDER = "foqos_reminder_channel"
        
        const val NOTIF_ID_SESSION = 100
        const val NOTIF_ID_REMINDER = 200
        
        const val ACTION_STOP_SESSION = "com.foqos.STOP_SESSION"
        const val ACTION_TAKE_BREAK = "com.foqos.TAKE_BREAK"
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels for Android O+.
     */
    private fun createNotificationChannels() {
        val sessionChannel = NotificationChannel(
            CHANNEL_SESSION,
            "Focus Sessions",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for active focus sessions"
            setShowBadge(true)
        }
        
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            "Session Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminder notifications during sessions"
            setShowBadge(true)
            enableVibration(true)
        }
        
        notificationManager.createNotificationChannel(sessionChannel)
        notificationManager.createNotificationChannel(reminderChannel)
    }
    
    /**
     * Build notification for active session.
     */
    fun buildSessionNotification(
        profileName: String,
        elapsedTime: String,
        canBreak: Boolean = false
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Focus Session Active")
            .setContentText("$profileName • $elapsedTime")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        // Add stop action
        val stopIntent = Intent(ACTION_STOP_SESSION)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.mipmap.ic_launcher,
            "Stop",
            stopPendingIntent
        )
        
        // Add break action if enabled
        if (canBreak) {
            val breakIntent = Intent(ACTION_TAKE_BREAK)
            val breakPendingIntent = PendingIntent.getBroadcast(
                context, 0, breakIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                R.mipmap.ic_launcher,
                "Break",
                breakPendingIntent
            )
        }
        
        return builder.build()
    }
    
    /**
     * Show session started notification.
     */
    fun showSessionStarted(profileName: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Session Started")
            .setContentText("Blocking apps for $profileName")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIF_ID_SESSION + 1, notification)
    }
    
    /**
     * Show session ended notification.
     */
    fun showSessionEnded(profileName: String, duration: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Session Complete")
            .setContentText("Focused for $duration on $profileName")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIF_ID_SESSION + 2, notification)
    }
    
    /**
     * Show reminder notification.
     */
    fun showReminder(message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Focus Reminder")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(NOTIF_ID_REMINDER, notification)
    }
    
    /**
     * Cancel all notifications.
     */
    fun cancelAll() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cancel session notification.
     */
    fun cancelSessionNotification() {
        notificationManager.cancel(NOTIF_ID_SESSION)
    }
}
