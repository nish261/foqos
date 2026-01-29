package com.foqos.domain.model

import kotlinx.serialization.Serializable

/**
 * NFC tag configuration for different unlock modes.
 */
@Serializable
data class NFCTagConfig(
    val tagId: String,
    val mode: NFCTagMode,
    val label: String? = null, // User-friendly name like "Desk Tag", "Emergency Tag"
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Different modes for NFC tags.
 */
@Serializable
enum class NFCTagMode {
    /**
     * Primary unlock - Ends the session completely
     */
    UNLOCK,
    
    /**
     * Pause mode - Temporarily pauses the session
     */
    PAUSE,
    
    /**
     * Resume mode - Resumes a paused session
     */
    RESUME,
    
    /**
     * Emergency unlock - Bypass with limited attempts
     */
    EMERGENCY,
    
    /**
     * Remote lock toggle - Can enable/disable remote lock mode
     */
    REMOTE_LOCK_TOGGLE,
    
    /**
     * Custom action - For future extensibility
     */
    CUSTOM
}
