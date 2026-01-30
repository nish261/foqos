package com.foqos.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.foqos.R
import com.foqos.data.repository.SessionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionRepository: SessionRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val sessionId = inputData.getString(KEY_SESSION_ID) ?: return Result.failure()
            val message = inputData.getString(KEY_MESSAGE) ?: DEFAULT_MESSAGE

            // Check if session is still active
            val session = sessionRepository.getSessionById(sessionId)
            if (session == null || session.endTime != null) {
                // Session ended, stop reminders
                return Result.success()
            }

            // Show reminder notification
            showReminderNotification(message)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showReminderNotification(message: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Focus Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Periodic reminders during focus sessions"
        }
        notificationManager.createNotificationChannel(channel)

        // Create intent to open app
        val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Stay Focused!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val KEY_MESSAGE = "message"
        const val WORK_NAME_PREFIX = "reminder_"
        const val CHANNEL_ID = "focus_reminders"
        const val NOTIFICATION_ID = 1002
        const val DEFAULT_MESSAGE = "You're doing great! Keep focusing on what matters."
    }
}
