package com.foqos.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.foqos.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handler for deep links to profiles.
 * Supports both foqos:// and https://foqos.app/ schemes.
 */
@Singleton
class DeepLinkHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Parse a deep link URI and extract profile ID.
     * Supports:
     * - foqos://profile/{id}
     * - https://foqos.app/profile/{id}
     */
    fun parseDeepLink(uri: Uri): DeepLinkResult {
        return try {
            val pathSegments = uri.pathSegments
            
            if (pathSegments.size >= 2 && pathSegments[0] == "profile") {
                val profileId = pathSegments[1]
                DeepLinkResult.ProfileLink(profileId)
            } else {
                DeepLinkResult.Invalid
            }
        } catch (e: Exception) {
            DeepLinkResult.Invalid
        }
    }
    
    /**
     * Create an intent to open a profile.
     */
    fun createProfileIntent(profileId: String): Intent {
        return Intent(context, MainActivity::class.java).apply {
            putExtra("profile_id", profileId)
            putExtra("action", "open_profile")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
    
    /**
     * Create a deep link URI for a profile.
     */
    fun createProfileDeepLink(profileId: String): Uri {
        return Uri.parse("https://foqos.app/profile/$profileId")
    }
    
    sealed class DeepLinkResult {
        data class ProfileLink(val profileId: String) : DeepLinkResult()
        object Invalid : DeepLinkResult()
    }
}
