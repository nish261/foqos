package com.foqos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.foqos.data.repository.SessionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TimerExpirationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionRepository: SessionRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val sessionId = inputData.getString(KEY_SESSION_ID) ?: return Result.failure()

            // Get the session
            val session = sessionRepository.getSessionById(sessionId)

            // Only end if session is still active (not already ended manually)
            if (session != null && session.endTime == null) {
                sessionRepository.endSession(sessionId)
            }

            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            Result.retry()
        }
    }

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val WORK_NAME_PREFIX = "timer_expiration_"
    }
}
