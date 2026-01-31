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
            _actionResult.emit(NFCActionResult.Error("Unknown NFC tag. Write this tag to the profile first."))
            return
        }

        // Only handle UNLOCK mode in minimal version
        if (matchingTag.mode == NFCTagMode.UNLOCK) {
            sessionRepository.endSession(activeSession.id)
            _actionResult.emit(NFCActionResult.Success("Session unlocked with NFC tag"))
        } else {
            _actionResult.emit(NFCActionResult.Error("This tag mode is not supported"))
        }
    }
}

sealed class NFCActionResult {
    data class Success(val message: String) : NFCActionResult()
    data class Error(val message: String) : NFCActionResult()
}
