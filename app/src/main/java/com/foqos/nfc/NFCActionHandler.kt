package com.foqos.nfc

import android.nfc.Tag
import com.foqos.data.repository.ProfileRepository
import com.foqos.data.repository.SessionRepository
import com.foqos.domain.model.NFCTagMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NFCActionHandler @Inject constructor(
    private val nfcReader: NFCReader,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository
) {

    private val _actionResult = MutableSharedFlow<NFCActionResult>(
        extraBufferCapacity = 1
    )
    val actionResult = _actionResult.asSharedFlow()

    suspend fun handleTag(tag: Tag) {
        val tagId = nfcReader.getTagId(tag)

        // Get active session
        val activeSession = sessionRepository.getActiveSession() ?: run {
            _actionResult.emit(NFCActionResult.Error("No active session"))
            return
        }

        // Get profile and its configured NFC tags
        val profile = profileRepository.getProfileById(activeSession.profileId) ?: run {
            _actionResult.emit(NFCActionResult.Error("Profile not found"))
            return
        }

        val configuredTags = profileRepository.getNFCTags(profile.id)
        val matchingTag = configuredTags.firstOrNull { it.tagId == tagId }

        if (matchingTag == null) {
            _actionResult.emit(NFCActionResult.Error("Unknown NFC tag"))
            return
        }

        // Handle the action based on the tag mode
        when (matchingTag.mode) {
            NFCTagMode.UNLOCK -> {
                sessionRepository.endSession(activeSession.id)
                _actionResult.emit(NFCActionResult.Success("Session ended"))
            }

            NFCTagMode.PAUSE -> {
                if (activeSession.breakStartTime != null) {
                    _actionResult.emit(NFCActionResult.Error("Already paused"))
                } else {
                    sessionRepository.startBreak(activeSession.id)
                    _actionResult.emit(NFCActionResult.Success("Session paused"))
                }
            }

            NFCTagMode.RESUME -> {
                if (activeSession.breakStartTime == null) {
                    _actionResult.emit(NFCActionResult.Error("Not paused"))
                } else {
                    sessionRepository.endBreak(activeSession.id)
                    _actionResult.emit(NFCActionResult.Success("Session resumed"))
                }
            }

            NFCTagMode.EMERGENCY -> {
                // Emergency bypass - end session regardless of cooldowns
                sessionRepository.endSession(activeSession.id)
                _actionResult.emit(NFCActionResult.Success("Emergency unlock activated"))
            }

            NFCTagMode.REMOTE_LOCK_TOGGLE -> {
                if (activeSession.remoteLockActivatedTime != null) {
                    // Deactivate remote lock
                    sessionRepository.deactivateRemoteLock(activeSession.id)
                    _actionResult.emit(NFCActionResult.Success("Remote lock deactivated"))
                } else {
                    // This shouldn't happen via NFC, but handle it gracefully
                    _actionResult.emit(NFCActionResult.Error("Remote lock not active"))
                }
            }

            NFCTagMode.CUSTOM -> {
                _actionResult.emit(NFCActionResult.Error("Custom tag mode not yet implemented"))
            }
        }
    }
}

sealed class NFCActionResult {
    data class Success(val message: String) : NFCActionResult()
    data class Error(val message: String) : NFCActionResult()
}
