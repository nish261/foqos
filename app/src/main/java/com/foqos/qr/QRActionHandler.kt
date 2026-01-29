package com.foqos.qr

import com.foqos.data.repository.ProfileRepository
import com.foqos.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRActionHandler @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository
) {

    private val _actionResult = MutableSharedFlow<QRActionResult>(
        extraBufferCapacity = 1
    )
    val actionResult = _actionResult.asSharedFlow()

    suspend fun handleQRCode(qrContent: String) {
        // Extract profile ID from QR code
        // Format: https://foqos.app/profile/{profileId} or foqos://profile/{profileId}
        val profileId = extractProfileId(qrContent)

        if (profileId == null) {
            _actionResult.emit(QRActionResult.Error("Invalid QR code"))
            return
        }

        // Get active session
        val activeSession = sessionRepository.getActiveSession() ?: run {
            _actionResult.emit(QRActionResult.Error("No active session"))
            return
        }

        // Get profile
        val profile = profileRepository.getProfileById(activeSession.profileId) ?: run {
            _actionResult.emit(QRActionResult.Error("Profile not found"))
            return
        }

        // Check if QR code matches the profile
        val expectedQRCodeId = profile.qrCodeId ?: profile.strictUnlockQRCode

        if (expectedQRCodeId == null) {
            _actionResult.emit(QRActionResult.Error("No QR code configured for this profile"))
            return
        }

        if (profileId != expectedQRCodeId && profileId != activeSession.profileId) {
            _actionResult.emit(QRActionResult.Error("QR code does not match this session"))
            return
        }

        // End the session
        sessionRepository.endSession(activeSession.id)
        _actionResult.emit(QRActionResult.Success("Session ended successfully"))
    }

    private fun extractProfileId(qrContent: String): String? {
        return when {
            qrContent.startsWith("https://foqos.app/profile/") -> {
                qrContent.removePrefix("https://foqos.app/profile/")
            }
            qrContent.startsWith("foqos://profile/") -> {
                qrContent.removePrefix("foqos://profile/")
            }
            else -> null
        }
    }
}

sealed class QRActionResult {
    data class Success(val message: String) : QRActionResult()
    data class Error(val message: String) : QRActionResult()
}
