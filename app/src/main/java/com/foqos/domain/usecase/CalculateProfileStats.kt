package com.foqos.domain.usecase

import com.foqos.data.local.entity.BlockedProfileSessionEntity
import com.foqos.data.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class ProfileStats(
    val totalSessions: Int,
    val totalFocusTimeMillis: Long,
    val lastUsedTimestamp: Long?,
    val successRate: Float, // Percentage of sessions completed without emergency unlock
    val averageSessionDurationMillis: Long
)

@Singleton
class CalculateProfileStats @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    /**
     * Calculate statistics for a specific profile
     */
    fun invoke(profileId: String): Flow<ProfileStats> {
        return sessionRepository.getSessionsForProfile(profileId).map { sessions ->
            calculateStats(sessions)
        }
    }

    /**
     * Calculate statistics for all sessions
     */
    fun invokeForAllSessions(): Flow<ProfileStats> {
        return sessionRepository.getAllCompletedSessions().map { sessions ->
            calculateStats(sessions)
        }
    }

    private fun calculateStats(sessions: List<BlockedProfileSessionEntity>): ProfileStats {
        val completedSessions = sessions.filter { it.endTime != null }

        if (completedSessions.isEmpty()) {
            return ProfileStats(
                totalSessions = 0,
                totalFocusTimeMillis = 0,
                lastUsedTimestamp = null,
                successRate = 0f,
                averageSessionDurationMillis = 0
            )
        }

        // Total sessions count
        val totalSessions = completedSessions.size

        // Total focus time (sum of all session durations)
        val totalFocusTime = completedSessions.sumOf { it.getTotalDuration() }

        // Last used timestamp (most recent session start time)
        val lastUsed = completedSessions.maxOfOrNull { it.startTime }

        // Success rate: sessions completed without emergency unlock
        val successfulSessions = completedSessions.count { session ->
            // Session is successful if it was NOT ended by emergency unlock
            // We can infer emergency unlock was used if emergencyUnlockAttemptsUsed > 0
            session.emergencyUnlockAttemptsUsed == 0
        }
        val successRate = if (totalSessions > 0) {
            (successfulSessions.toFloat() / totalSessions.toFloat()) * 100f
        } else {
            0f
        }

        // Average session duration
        val averageDuration = if (totalSessions > 0) {
            totalFocusTime / totalSessions
        } else {
            0L
        }

        return ProfileStats(
            totalSessions = totalSessions,
            totalFocusTimeMillis = totalFocusTime,
            lastUsedTimestamp = lastUsed,
            successRate = successRate,
            averageSessionDurationMillis = averageDuration
        )
    }
}
