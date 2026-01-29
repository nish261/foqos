package com.foqos.presentation.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foqos.data.repository.ProfileRepository
import com.foqos.domain.model.NFCTagConfig
import com.foqos.domain.model.NFCTagMode
import com.foqos.nfc.NFCReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NFCTagManagementViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val nfcReader: NFCReader
) : ViewModel() {

    private val _nfcTags = MutableStateFlow<List<NFCTagConfig>>(emptyList())
    val nfcTags: StateFlow<List<NFCTagConfig>> = _nfcTags.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _uiState = MutableStateFlow<NFCTagManagementUiState>(NFCTagManagementUiState.Ready)
    val uiState: StateFlow<NFCTagManagementUiState> = _uiState.asStateFlow()

    private var currentProfileId: String? = null
    private var currentMode: NFCTagMode? = null

    init {
        // Listen for NFC tags when scanning
        viewModelScope.launch {
            nfcReader.tagFlow.collect { tag ->
                if (_isScanning.value && currentProfileId != null && currentMode != null) {
                    val tagId = tag.id.joinToString("") { "%02x".format(it) }
                    addTag(currentProfileId!!, currentMode!!, tagId)
                    _isScanning.value = false
                    currentMode = null
                }
            }
        }
    }

    fun loadTags(profileId: String) {
        currentProfileId = profileId
        viewModelScope.launch {
            val tags = profileRepository.getNFCTags(profileId)
            _nfcTags.value = tags
        }
    }

    fun startScanning(profileId: String, mode: NFCTagMode) {
        currentProfileId = profileId
        currentMode = mode
        _isScanning.value = true
    }

    private suspend fun addTag(profileId: String, mode: NFCTagMode, tagId: String) {
        try {
            val tag = NFCTagConfig(
                tagId = tagId,
                mode = mode,
                label = null
            )
            profileRepository.addNFCTag(profileId, tag)
            loadTags(profileId)
            _uiState.value = NFCTagManagementUiState.Success("Tag added successfully")
        } catch (e: Exception) {
            _uiState.value = NFCTagManagementUiState.Error(e.message ?: "Failed to add tag")
        }
    }

    fun removeTag(profileId: String, tagId: String) {
        viewModelScope.launch {
            try {
                profileRepository.removeNFCTag(profileId, tagId)
                loadTags(profileId)
                _uiState.value = NFCTagManagementUiState.Success("Tag removed")
            } catch (e: Exception) {
                _uiState.value = NFCTagManagementUiState.Error(e.message ?: "Failed to remove tag")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = NFCTagManagementUiState.Ready
    }
}

sealed class NFCTagManagementUiState {
    object Ready : NFCTagManagementUiState()
    data class Success(val message: String) : NFCTagManagementUiState()
    data class Error(val message: String) : NFCTagManagementUiState()
}
