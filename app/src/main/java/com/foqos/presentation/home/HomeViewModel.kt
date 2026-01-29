package com.foqos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.data.local.entity.BlockedProfileSessionEntity
import com.foqos.data.repository.ProfileRepository
import com.foqos.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
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
    
    fun startSession(profile: BlockedProfileEntity) {
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
                sessionRepository.startSession(
                    profileId = profile.id,
                    strategyId = profile.blockingStrategyId,
                    blockedApps = profile.selectedApps,
                    blockedDomains = profile.domains ?: emptyList()
                )
                
                _uiState.value = HomeUiState.Success("Session started")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to start session")
            }
        }
    }
    
    fun stopSession() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                val session = sessionRepository.getActiveSession()
                if (session != null) {
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
}

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val message: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
