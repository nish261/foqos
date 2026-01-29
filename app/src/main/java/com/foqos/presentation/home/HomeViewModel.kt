package com.foqos.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.data.local.entity.BlockedProfileSessionEntity
import com.foqos.data.repository.ProfileRepository
import com.foqos.presentation.components.TodayStats
import com.foqos.worker.TimerExpirationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.foqos.data.repository.SessionRepository
import com.foqos.nfc.NFCActionHandler
import com.foqos.nfc.NFCActionResult
import com.foqos.nfc.NFCReader
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val nfcReader: NFCReader,
    private val nfcActionHandler: NFCActionHandler
) : ViewModel() {

    init {
        // Listen for NFC tags
        viewModelScope.launch {
            nfcReader.tagScanned.collect { tag ->
                // Only process if there's an active session
                if (activeSession.value != null) {
                    nfcActionHandler.handleTag(tag)
                }
            }
        }

        // Listen for NFC action results
        viewModelScope.launch {
            nfcActionHandler.actionResult.collect { result ->
                when (result) {
                    is NFCActionResult.Success -> _uiState.value = HomeUiState.Success(result.message)
                    is NFCActionResult.Error -> _uiState.value = HomeUiState.Error(result.message)
                }
            }
        }
    }
    
    val profiles: StateFlow<List<BlockedProfileEntity>> = profileRepository
        .getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val activeSession: StateFlow<BlockedProfileSessionEntity?> = sessionRepository
        .getActiveSessionFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Completed session dates for calendar
    val completedSessionDates: StateFlow<List<Long>> = sessionRepository
        .getAllCompletedSessions()
        .map { sessions -> sessions.map { it.startTime } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Today's stats
    val todayStats: StateFlow<TodayStats> = combine(
        activeSession,
        sessionRepository.getAllCompletedSessions()
    ) { session, allSessions ->
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val todaySessions = allSessions.filter { it.startTime in todayStart..todayEnd }
        val focusTime = todaySessions.sumOf { it.getTotalDuration() }

        TodayStats(
            blockedAppsCount = session?.blockedApps?.size ?: 0,
            blockedDomainsCount = session?.blockedDomains?.size ?: 0,
            totalSessions = todaySessions.size,
            focusTimeMills = focusTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayStats(0, 0, 0, 0)
    )

    // Session counts per profile
    val profileSessionCounts: StateFlow<Map<String, Int>> = sessionRepository
        .getAllCompletedSessions()
        .map { sessions ->
            sessions.groupBy { it.profileId }
                .mapValues { it.value.size }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
    
    fun createProfile(name: String) {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                profileRepository.createProfile(name = name)
                _uiState.value = HomeUiState.Success("Profile created")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to create profile")
            }
        }
    }
    
    fun deleteProfile(profile: BlockedProfileEntity) {
        viewModelScope.launch {
            try {
                // Delete all sessions for this profile first
                sessionRepository.deleteSessionsForProfile(profile.id)
                // Then delete the profile
                profileRepository.deleteProfile(profile)
                _uiState.value = HomeUiState.Success("Profile deleted")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to delete profile")
            }
        }
    }
    
    fun startSession(profile: BlockedProfileEntity, timerDurationMinutes: Int? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading

                // Check if there's already an active session
                val existingSession = sessionRepository.getActiveSession()
                if (existingSession != null) {
                    _uiState.value = HomeUiState.Error("A session is already active")
                    return@launch
                }

                // Start new session
                val session = sessionRepository.startSession(
                    profileId = profile.id,
                    strategyId = profile.blockingStrategyId,
                    blockedApps = profile.selectedApps,
                    blockedDomains = profile.domains ?: emptyList(),
                    timerDurationMinutes = timerDurationMinutes
                )

                // Schedule timer expiration worker if timer is set
                if (timerDurationMinutes != null) {
                    scheduleTimerExpiration(session.id, timerDurationMinutes)
                }

                _uiState.value = HomeUiState.Success("Session started")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to start session")
            }
        }
    }

    private fun scheduleTimerExpiration(sessionId: String, durationMinutes: Int) {
        val workRequest = OneTimeWorkRequestBuilder<TimerExpirationWorker>()
            .setInitialDelay(durationMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(
                workDataOf(TimerExpirationWorker.KEY_SESSION_ID to sessionId)
            )
            .build()

        WorkManager.getInstance(context)
            .enqueue(workRequest)
    }

    fun cancelTimer(sessionId: String) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("${TimerExpirationWorker.WORK_NAME_PREFIX}$sessionId")
    }
    
    fun stopSession() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                val session = sessionRepository.getActiveSession()
                if (session != null) {
                    // Check if remote lock is active
                    if (session.remoteLockActivatedTime != null) {
                        _uiState.value = HomeUiState.Error("Remote lock active. Use NFC to unlock")
                        return@launch
                    }

                    sessionRepository.endSession(session.id)
                    _uiState.value = HomeUiState.Success("Session ended")
                } else {
                    _uiState.value = HomeUiState.Error("No active session")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to stop session")
            }
        }
    }
    
    fun clearUiState() {
        _uiState.value = HomeUiState.Idle
    }

    fun startBreak(duration: Int) {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getActiveSession()
                if (session == null) {
                    _uiState.value = HomeUiState.Error("No active session")
                    return@launch
                }

                if (session.breakStartTime != null) {
                    _uiState.value = HomeUiState.Error("Already on break")
                    return@launch
                }

                sessionRepository.startBreak(session.id)
                _uiState.value = HomeUiState.Success("Break started")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to start break")
            }
        }
    }

    fun endBreak() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getActiveSession()
                if (session == null) {
                    _uiState.value = HomeUiState.Error("No active session")
                    return@launch
                }

                if (session.breakStartTime == null) {
                    _uiState.value = HomeUiState.Error("Not on break")
                    return@launch
                }

                sessionRepository.endBreak(session.id)
                _uiState.value = HomeUiState.Success("Break ended")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to end break")
            }
        }
    }

    fun useEmergencyUnlock() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getActiveSession()
                if (session == null) {
                    _uiState.value = HomeUiState.Error("No active session")
                    return@launch
                }

                // Get profile to check max attempts
                val profile = profileRepository.getProfileById(session.profileId)
                if (profile == null || !profile.emergencyUnlockEnabled) {
                    _uiState.value = HomeUiState.Error("Emergency unlock not enabled")
                    return@launch
                }

                // Check cooldown
                val now = System.currentTimeMillis()
                if (session.emergencyUnlockCooldownUntil != null && now < session.emergencyUnlockCooldownUntil) {
                    val remainingMinutes = ((session.emergencyUnlockCooldownUntil - now) / 60000).toInt()
                    _uiState.value = HomeUiState.Error("Cooldown active. Wait $remainingMinutes more minutes")
                    return@launch
                }

                // Check attempts remaining
                val attemptsUsed = session.emergencyUnlockAttemptsUsed
                val maxAttempts = profile.emergencyUnlockAttempts

                if (attemptsUsed >= maxAttempts) {
                    // Apply cooldown
                    val cooldownUntil = now + (profile.emergencyUnlockCooldownMinutes * 60000L)
                    sessionRepository.updateEmergencyUnlockAttempts(session.id, attemptsUsed, cooldownUntil)
                    _uiState.value = HomeUiState.Error("All attempts used. ${profile.emergencyUnlockCooldownMinutes}min cooldown activated")
                    return@launch
                }

                // Use attempt
                val newAttemptsUsed = attemptsUsed + 1
                val cooldownUntil = if (newAttemptsUsed >= maxAttempts) {
                    now + (profile.emergencyUnlockCooldownMinutes * 60000L)
                } else null

                sessionRepository.updateEmergencyUnlockAttempts(session.id, newAttemptsUsed, cooldownUntil)

                // End session
                sessionRepository.endSession(session.id)

                val remaining = maxAttempts - newAttemptsUsed
                val message = if (remaining > 0) {
                    "Emergency unlock used. $remaining attempts remaining"
                } else {
                    "Last emergency attempt used. Cooldown activated"
                }
                _uiState.value = HomeUiState.Success(message)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Emergency unlock failed")
            }
        }
    }

    fun activateRemoteLock(deviceName: String) {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getActiveSession()
                if (session == null) {
                    _uiState.value = HomeUiState.Error("No active session")
                    return@launch
                }

                // Check if profile has NFC tags configured
                val profile = profileRepository.getProfileById(session.profileId)
                if (profile?.nfcTagsJson.isNullOrBlank()) {
                    _uiState.value = HomeUiState.Error("No NFC tags configured. Add NFC unlock tag first")
                    return@launch
                }

                if (profile?.remoteLockEnabled != true) {
                    _uiState.value = HomeUiState.Error("Remote lock not enabled in profile")
                    return@launch
                }

                sessionRepository.activateRemoteLock(session.id, deviceName)
                _uiState.value = HomeUiState.Success("Remote lock activated. NFC required to unlock")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to activate remote lock")
            }
        }
    }

    fun pauseSession() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getActiveSession()
                if (session == null) {
                    _uiState.value = HomeUiState.Error("No active session")
                    return@launch
                }

                if (session.breakStartTime != null) {
                    // Already paused, resume
                    sessionRepository.endBreak(session.id)
                    _uiState.value = HomeUiState.Success("Session resumed")
                } else {
                    // Not paused, pause
                    sessionRepository.startBreak(session.id)
                    _uiState.value = HomeUiState.Success("Session paused")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to pause/resume session")
            }
        }
    }
}

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val message: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
