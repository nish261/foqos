package com.foqos.domain.usecase

import com.foqos.data.repository.ProfileRepository
import com.foqos.data.repository.SessionRepository
import com.foqos.domain.model.NFCTagMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles emergency unlock logic with attempt tracking.
 */
@Singleton
class EmergencyUnlockManager @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository
) {
    
    /**
     * Check if emergency unlock is available.
     * Returns (canUse, attemptsRemaining, cooldownEndTime)
     */
    suspend fun checkAvailability(): EmergencyUnlockStatus {
        val session = sessionRepository.getActiveSession()
            ?: return EmergencyUnlockStatus.NoActiveSession
        
        val profile = profileRepository.getProfileById(session.profileId)
            ?: return EmergencyUnlockStatus.NoActiveSession
        
        if (!profile.emergencyUnlockEnabled) {
            return EmergencyUnlockStatus.Disabled
        }
        
        // Check cooldown
        val cooldownEnd = session.emergencyUnlockCooldownUntil
        if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
            return EmergencyUnlockStatus.OnCooldown(cooldownEnd)
        }
        
        val attemptsRemaining = profile.emergencyUnlockAttempts - session.emergencyUnlockAttemptsUsed
        
        return if (attemptsRemaining > 0) {
            EmergencyUnlockStatus.Available(attemptsRemaining, profile.emergencyUnlockAttempts)
        } else {
            EmergencyUnlockStatus.NoAttemptsLeft
        }
    }
    
    /**
     * Use one emergency unlock attempt.
     */
    suspend fun useEmergencyUnlock(): Result<Unit> {
        val session = sessionRepository.getActiveSession()
            ?: return Result.failure(Exception("No active session"))
        
        val profile = profileRepository.getProfileById(session.profileId)
            ?: return Result.failure(Exception("Profile not found"))
        
        val attemptsRemaining = profile.emergencyUnlockAttempts - session.emergencyUnlockAttemptsUsed
        
        if (attemptsRemaining <= 0) {
            return Result.failure(Exception("No emergency unlocks remaining"))
        }
        
        // Increment attempts
        val newAttemptsUsed = session.emergencyUnlockAttemptsUsed + 1
        val cooldownEnd = if (newAttemptsUsed >= profile.emergencyUnlockAttempts) {
            // All attempts used, set cooldown
            System.currentTimeMillis() + (profile.emergencyUnlockCooldownMinutes * 60 * 1000)
        } else {
            null
        }
        
        // Update session
        sessionRepository.updateEmergencyUnlockAttempts(
            sessionId = session.id,
            attemptsUsed = newAttemptsUsed,
            cooldownUntil = cooldownEnd
        )
        
        // End the session
        sessionRepository.endSession(session.id)
        
        return Result.success(Unit)
    }
}

sealed class EmergencyUnlockStatus {
    object NoActiveSession : EmergencyUnlockStatus()
    object Disabled : EmergencyUnlockStatus()
    object NoAttemptsLeft : EmergencyUnlockStatus()
    data class OnCooldown(val endTime: Long) : EmergencyUnlockStatus()
    data class Available(val attemptsRemaining: Int, val maxAttempts: Int) : EmergencyUnlockStatus()
}
