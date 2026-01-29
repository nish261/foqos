package com.foqos.presentation.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.data.repository.ProfileRepository
import com.foqos.domain.model.BlockingStrategy
import com.foqos.util.AppListProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val appListProvider: AppListProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val profileId: String? = savedStateHandle.get<String>("profile_id")
    
    private val _profile = MutableStateFlow<BlockedProfileEntity?>(null)
    val profile: StateFlow<BlockedProfileEntity?> = _profile.asStateFlow()
    
    private val _availableApps = MutableStateFlow<List<com.foqos.util.AppInfo>>(emptyList())
    val availableApps: StateFlow<List<com.foqos.util.AppInfo>> = _availableApps.asStateFlow()
    
    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()
    
    private val _selectedStrategy = MutableStateFlow<BlockingStrategy>(BlockingStrategy.NFCBlocking)
    val selectedStrategy: StateFlow<BlockingStrategy> = _selectedStrategy.asStateFlow()
    
    private val _domains = MutableStateFlow<List<String>>(emptyList())
    val domains: StateFlow<List<String>> = _domains.asStateFlow()
    
    private val _uiState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Loading)
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
        loadAvailableApps()
    }
    
    private fun loadProfile() {
        if (profileId != null) {
            viewModelScope.launch {
                val profile = profileRepository.getProfileById(profileId)
                if (profile != null) {
                    _profile.value = profile
                    _selectedApps.value = profile.selectedApps.toSet()
                    _selectedStrategy.value = BlockingStrategy.fromId(profile.blockingStrategyId)
                    _domains.value = profile.domains ?: emptyList()
                }
                _uiState.value = ProfileEditUiState.Ready
            }
        } else {
            // New profile
            _uiState.value = ProfileEditUiState.Ready
        }
    }
    
    private fun loadAvailableApps() {
        viewModelScope.launch {
            val apps = appListProvider.getUserApps()
            _availableApps.value = apps
        }
    }
    
    fun toggleApp(packageName: String) {
        val current = _selectedApps.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _selectedApps.value = current
    }
    
    fun setStrategy(strategy: BlockingStrategy) {
        _selectedStrategy.value = strategy
    }
    
    fun addDomain(domain: String) {
        val current = _domains.value.toMutableList()
        if (!current.contains(domain) && domain.isNotBlank()) {
            current.add(domain)
            _domains.value = current
        }
    }
    
    fun removeDomain(domain: String) {
        val current = _domains.value.toMutableList()
        current.remove(domain)
        _domains.value = current
    }
    
    fun saveProfile(name: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileEditUiState.Saving
                
                val profile = _profile.value
                if (profile != null) {
                    // Update existing
                    profileRepository.updateProfile(
                        profile.copy(
                            name = name,
                            selectedApps = _selectedApps.value.toList(),
                            blockingStrategyId = _selectedStrategy.value.id,
                            domains = _domains.value.takeIf { it.isNotEmpty() }
                        )
                    )
                } else {
                    // Create new
                    profileRepository.createProfile(
                        name = name,
                        selectedApps = _selectedApps.value.toList(),
                        blockingStrategyId = _selectedStrategy.value.id,
                        domains = _domains.value.takeIf { it.isNotEmpty() }
                    )
                }
                
                _uiState.value = ProfileEditUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ProfileEditUiState.Error(e.message ?: "Failed to save profile")
            }
        }
    }
}

sealed class ProfileEditUiState {
    object Loading : ProfileEditUiState()
    object Ready : ProfileEditUiState()
    object Saving : ProfileEditUiState()
    object Saved : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}
