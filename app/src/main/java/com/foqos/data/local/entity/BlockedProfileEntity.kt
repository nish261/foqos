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
    val selectedApps: List<String> = emptyList(),
    val domains: List<String>? = null,
    val blockingStrategyId: String = "nfc",
    val nfcTagsJson: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
