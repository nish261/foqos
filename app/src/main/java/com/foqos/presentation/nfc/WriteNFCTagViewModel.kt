package com.foqos.presentation.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foqos.data.repository.ProfileRepository
import com.foqos.nfc.NFCReader
import com.foqos.nfc.NFCWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteNFCTagViewModel @Inject constructor(
    private val nfcReader: NFCReader,
    private val nfcWriter: NFCWriter,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _writeState = MutableStateFlow<WriteNFCTagState>(WriteNFCTagState.Ready)
    val writeState: StateFlow<WriteNFCTagState> = _writeState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var currentProfileId: String? = null

    init {
        // Listen for NFC tags
        viewModelScope.launch {
            nfcReader.tagFlow.collect { tag ->
                if (_isScanning.value && currentProfileId != null) {
                    writeTagToProfile(tag)
                }
            }
        }
    }

    fun startWriting(profileId: String) {
        currentProfileId = profileId
        _isScanning.value = true
        _writeState.value = WriteNFCTagState.Ready
    }

    private fun writeTagToProfile(tag: android.nfc.Tag) {
        viewModelScope.launch {
            try {
                _writeState.value = WriteNFCTagState.Writing
                _isScanning.value = false

                val profileId = currentProfileId ?: return@launch

                // Write profile ID to tag
                val writeResult = nfcWriter.writeProfileToTag(tag, profileId)

                when (writeResult) {
                    is NFCWriter.WriteResult.Success -> {
                        // Store the tag ID as strict unlock tag
                        val tagId = nfcReader.getTagId(tag)
                        val profile = profileRepository.getProfileById(profileId)

                        if (profile != null) {
                            profileRepository.updateProfile(
                                profile.copy(strictUnlockTagId = tagId)
                            )
                        }

                        _writeState.value = WriteNFCTagState.Success
                    }
                    is NFCWriter.WriteResult.Error -> {
                        _writeState.value = WriteNFCTagState.Error(writeResult.message)
                    }
                }
            } catch (e: Exception) {
                _writeState.value = WriteNFCTagState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class WriteNFCTagState {
    object Ready : WriteNFCTagState()
    object Writing : WriteNFCTagState()
    object Success : WriteNFCTagState()
    data class Error(val message: String) : WriteNFCTagState()
}
