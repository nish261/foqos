package com.foqos.domain.usecase

import com.foqos.data.repository.ProfileRepository
import com.foqos.data.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages remote lock functionality.
 */
@Singleton
class RemoteLockManager @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository
) {
    
    /**
     * Activate remote lock for the active session.
     * Once activated, session can only be ended via NFC.
     */
    suspend fun activateRemoteLock(activatedBy: String? = null): Result<Unit> {
        val session = sessionRepository.getActiveSession()
            ?: return Result.failure(Exception("No active session"))
        
        val profile = profileRepository.getProfileById(session.profileId)
            ?: return Result.failure(Exception("Profile not found"))
        
        if (!profile.remoteLockEnabled) {
            return Result.failure(Exception("Remote lock not enabled for this profile"))
        }
        
        if (profile.nfcTags.isNullOrEmpty()) {
            return Result.failure(Exception("No NFC tags configured"))
        }
        
        // Update session to mark remote lock as active
        sessionRepository.activateRemoteLock(
            sessionId = session.id,
            activatedBy = activatedBy
        )
        
        return Result.success(Unit)
    }
    
    /**
     * Deactivate remote lock (requires NFC tag).
     */
    suspend fun deactivateRemoteLock(): Result<Unit> {
        val session = sessionRepository.getActiveSession()
            ?: return Result.failure(Exception("No active session"))
        
        sessionRepository.deactivateRemoteLock(session.id)
        
        return Result.success(Unit)
    }
    
    /**
     * Check if remote lock is currently active.
     */
    suspend fun isRemoteLockActive(): Boolean {
        val session = sessionRepository.getActiveSession() ?: return false
        return session.remoteLockActivatedTime != null
    }
    
    /**
     * Check if profile supports remote lock.
     */
    suspend fun isRemoteLockSupported(profileId: String): Boolean {
        val profile = profileRepository.getProfileById(profileId) ?: return false
        return profile.remoteLockEnabled && !profile.nfcTags.isNullOrEmpty()
    }
}
