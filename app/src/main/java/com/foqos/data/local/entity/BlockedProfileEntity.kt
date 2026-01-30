package com.foqos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.foqos.data.local.Converters
import java.util.UUID

@Entity(tableName = "blocked_profiles")
@TypeConverters(Converters::class)
data class BlockedProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val selectedApps: List<String> = emptyList(), // Package names
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val blockingStrategyId: String = "nfc",
    val strategyData: String? = null, // JSON serialized strategy-specific data
    val order: Int = 0,
    
    // Features
    val enableLiveNotification: Boolean = true,
    val reminderTimeInSeconds: Int? = null,
    val customReminderMessage: String? = null,
    val enableBreaks: Boolean = false,
    val breakTimeInMinutes: Int = 15,
    val enableStrictMode: Boolean = false,
    val enableWebBlocking: Boolean = true,

    // Allow Mode (Inverse Blocking)
    val appsAllowMode: Boolean = false,           // If true, only allow selected apps (block everything else)
    val domainsAllowMode: Boolean = false,        // If true, only allow selected domains (block everything else)

    // Block All Browsers
    val blockAllBrowsers: Boolean = false,        // If true, automatically block all browser apps
    
    // Website blocking
    val domains: List<String>? = null,
    
    // Physical unlock - Multiple NFC tags with different purposes (JSON serialized)
    val nfcTagsJson: String? = null, // Stored as JSON string of List<NFCTagConfig>

    // Strict unlock - Specific NFC tag required to unlock (not any tag)
    val strictUnlockTagId: String? = null, // Single NFC tag ID for strict unlock

    // QR code unlock
    val qrCodeId: String? = null,
    val strictUnlockQRCode: String? = null, // Specific QR code for strict unlock

    // UI customization
    val gradientId: Int = 0 // 0-7 for preset gradient colors
    
    // Emergency unlock settings
    val emergencyUnlockEnabled: Boolean = true,
    val emergencyUnlockAttempts: Int = 5,
    val emergencyUnlockCooldownMinutes: Int = 60, // Cooldown after all attempts used
    
    // Remote lock feature
    val remoteLockEnabled: Boolean = false, // Can be enabled remotely
    val remoteLockActive: Boolean = false, // Currently in remote lock mode
    
    // Schedule
    val scheduleEnabled: Boolean = false,
    val scheduleDaysOfWeek: List<Int>? = null, // 1=Monday, 7=Sunday
    val scheduleStartTime: String? = null, // HH:mm format
    val scheduleEndTime: String? = null, // HH:mm format
    
    val disableBackgroundStops: Boolean = false
)
