package com.foqos.data.repository

import com.foqos.data.local.dao.SessionDao
import com.foqos.data.local.entity.BlockedProfileSessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    fun getSessionsForProfile(profileId: String): Flow<List<BlockedProfileSessionEntity>> {
        return sessionDao.getSessionsForProfile(profileId)
    }
    
    suspend fun getActiveSession(): BlockedProfileSessionEntity? {
        return sessionDao.getActiveSession()
    }
    
    fun getActiveSessionFlow(): Flow<BlockedProfileSessionEntity?> {
        return sessionDao.getActiveSessionFlow()
    }
    
    suspend fun getSessionById(id: String): BlockedProfileSessionEntity? {
        return sessionDao.getSessionById(id)
    }
    
    fun getRecentSessions(limit: Int = 50): Flow<List<BlockedProfileSessionEntity>> {
        return sessionDao.getRecentSessions(limit)
    }

    fun getAllCompletedSessions(): Flow<List<BlockedProfileSessionEntity>> {
        return sessionDao.getAllCompletedSessions()
    }
    
    suspend fun insertSession(session: BlockedProfileSessionEntity) {
        sessionDao.insertSession(session)
    }
    
    suspend fun updateSession(session: BlockedProfileSessionEntity) {
        sessionDao.updateSession(session)
    }
    
    suspend fun deleteSession(session: BlockedProfileSessionEntity) {
        sessionDao.deleteSession(session)
    }
    
    suspend fun deleteSessionsForProfile(profileId: String) {
        sessionDao.deleteSessionsForProfile(profileId)
    }
    
    suspend fun startSession(
        profileId: String,
        strategyId: String,
        strategyStartData: String? = null,
        blockedApps: List<String> = emptyList(),
        blockedDomains: List<String> = emptyList(),
        timerDurationMinutes: Int? = null
    ): BlockedProfileSessionEntity {
        val session = BlockedProfileSessionEntity(
            profileId = profileId,
            startTime = System.currentTimeMillis(),
            strategyId = strategyId,
            strategyStartData = strategyStartData,
            blockedApps = blockedApps,
            blockedDomains = blockedDomains,
            timerDurationMinutes = timerDurationMinutes
        )
        sessionDao.insertSession(session)
        return session
    }
    
    suspend fun endSession(sessionId: String) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(endTime = System.currentTimeMillis())
        sessionDao.updateSession(updatedSession)
    }
    
    suspend fun startBreak(sessionId: String) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(
            breakStartTime = System.currentTimeMillis()
        )
        sessionDao.updateSession(updatedSession)
    }

    suspend fun endBreak(sessionId: String) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        if (session.breakStartTime != null) {
            val pauseDuration = BlockedProfileSessionEntity.PausedDuration(
                startTime = session.breakStartTime,
                endTime = System.currentTimeMillis()
            )
            val updatedSession = session.copy(
                breakStartTime = null,
                pausedDurations = session.pausedDurations + pauseDuration
            )
            sessionDao.updateSession(updatedSession)
        }
    }
    
    suspend fun updateEmergencyUnlockAttempts(
        sessionId: String,
        attemptsUsed: Int,
        cooldownUntil: Long?
    ) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(
            emergencyUnlockAttemptsUsed = attemptsUsed,
            emergencyUnlockCooldownUntil = cooldownUntil,
            lastEmergencyAttemptTime = System.currentTimeMillis()
        )
        sessionDao.updateSession(updatedSession)
    }

    suspend fun activateRemoteLock(sessionId: String, activatedBy: String?) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(
            remoteLockActivatedTime = System.currentTimeMillis(),
            remoteLockActivatedBy = activatedBy
        )
        sessionDao.updateSession(updatedSession)
    }

    suspend fun deactivateRemoteLock(sessionId: String) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(
            remoteLockActivatedTime = null,
            remoteLockActivatedBy = null
        )
        sessionDao.updateSession(updatedSession)
    }
}
