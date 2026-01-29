package com.foqos.domain.usecase

import com.foqos.data.repository.ProfileRepository
import com.foqos.data.repository.SessionRepository
import com.foqos.notification.FoqosNotificationManager
import com.foqos.service.BlockingForegroundService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages blocking sessions lifecycle.
 * Coordinates profile settings, session tracking, and services.
 */
@Singleton
class SessionManager @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val notificationManager: FoqosNotificationManager
) {
    
    /**
     * Start a new blocking session.
     */
    suspend fun startSession(profileId: String): Result<String> {
        return try {
            // Check if there's already an active session
            val existingSession = sessionRepository.getActiveSession()
            if (existingSession != null) {
                return Result.failure(Exception("A session is already active"))
            }
            
            // Get profile
            val profile = profileRepository.getProfileById(profileId)
                ?: return Result.failure(Exception("Profile not found"))
            
            // Create session
            val session = sessionRepository.startSession(
                profileId = profile.id,
                strategyId = profile.blockingStrategyId,
                blockedApps = profile.selectedApps,
                blockedDomains = profile.domains ?: emptyList()
            )
            
            // Start services
            // TODO: Start BlockingForegroundService
            // TODO: Start AppBlockerAccessibilityService if needed
            // TODO: Start WebsiteBlockerVpnService if website blocking enabled
            
            // Show notification
            notificationManager.showSessionStarted(profile.name)
            
            Result.success(session.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Stop the active session.
     */
    suspend fun stopSession(): Result<Unit> {
        return try {
            val session = sessionRepository.getActiveSession()
                ?: return Result.failure(Exception("No active session"))
            
            val profile = profileRepository.getProfileById(session.profileId)
            
            // End session
            sessionRepository.endSession(session.id)
            
            // Stop services
            // TODO: Stop BlockingForegroundService
            // TODO: Stop VPN service if running
            
            // Show completion notification
            if (profile != null) {
                val duration = com.foqos.util.TimeFormatter.formatDuration(
                    System.currentTimeMillis() - session.startTime
                )
                notificationManager.showSessionEnded(profile.name, duration)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Emergency stop - terminates session regardless of strategy.
     */
    suspend fun emergencyStop(): Result<Unit> {
        return stopSession()
    }
    
    /**
     * Start a break in the current session.
     */
    suspend fun startBreak(): Result<Unit> {
        return try {
            val session = sessionRepository.getActiveSession()
                ?: return Result.failure(Exception("No active session"))
            
            // TODO: Implement break tracking
            // For now, just pause the blocking
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * End a break and resume blocking.
     */
    suspend fun endBreak(): Result<Unit> {
        return try {
            val session = sessionRepository.getActiveSession()
                ?: return Result.failure(Exception("No active session"))
            
            // TODO: Implement break end tracking
            // Resume blocking
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a session is currently active.
     */
    suspend fun isSessionActive(): Boolean {
        return sessionRepository.getActiveSession() != null
    }
    
    /**
     * Get the currently active session ID.
     */
    suspend fun getActiveSessionId(): String? {
        return sessionRepository.getActiveSession()?.id
    }
}
