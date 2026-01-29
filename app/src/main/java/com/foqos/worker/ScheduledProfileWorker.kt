package com.foqos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.foqos.domain.usecase.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker for scheduled profile activation.
 * Triggered by AlarmManager/WorkManager for scheduled blocking.
 */
@HiltWorker
class ScheduledProfileWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val profileId = inputData.getString("profile_id") ?: return Result.failure()
        
        return try {
            val result = sessionManager.startSession(profileId)
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
