package com.foqos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.foqos.data.local.Converters
import java.util.UUID

@Entity(
    tableName = "blocked_sessions",
    foreignKeys = [
        ForeignKey(
            entity = BlockedProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
@TypeConverters(Converters::class)
data class BlockedProfileSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val pausedDurations: List<PausedDuration> = emptyList(),
    val strategyId: String,
    val strategyStartData: String? = null, // NFC tag ID or QR code content used to start
    val blockedApps: List<String> = emptyList(),
    val blockedDomains: List<String> = emptyList(),
    val timerDurationMinutes: Int? = null
) {
    data class PausedDuration(
        val startTime: Long,
        val endTime: Long
    )
    
    fun getTotalDuration(): Long {
        val end = endTime ?: System.currentTimeMillis()
        val totalPausedTime = pausedDurations.sumOf { it.endTime - it.startTime }
        return (end - startTime) - totalPausedTime
    }
    
    fun isActive(): Boolean = endTime == null
}
