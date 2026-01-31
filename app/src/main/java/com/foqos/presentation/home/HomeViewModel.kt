package com.foqos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.data.repository.ProfileRepository
import com.foqos.data.repository.SessionRepository
import com.foqos.nfc.NFCActionHandler
import com.foqos.nfc.NFCReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val nfcReader: NFCReader,
    private val nfcActionHandler: NFCActionHandler
) : ViewModel() {

    init {
        // Listen for NFC tags
        viewModelScope.launch {
            nfcReader.tag.collect { tag ->
                nfcActionHandler.handleTag(tag)
            }
        }

        // Listen for NFC action results
        viewModelScope.launch {
            nfcActionHandler.actionResult.collect { result ->
                when (result) {
                    is com.foqos.nfc.NFCActionResult.Success -> {
                        _uiState.value = HomeUiState.Success(result.message)
                    }
                    is com.foqos.nfc.NFCActionResult.Error -> {
                        _uiState.value = HomeUiState.Error(result.message)
                    }
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

    val activeSession = sessionRepository
        .getActiveSessionFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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

    fun deleteProfile(profile: BlockedProfileEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                profileRepository.deleteProfile(profile)
                _uiState.value = HomeUiState.Success("Profile deleted")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to delete profile")
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
