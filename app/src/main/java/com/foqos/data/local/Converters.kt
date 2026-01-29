package com.foqos.data.local

import androidx.room.TypeConverter
import com.foqos.data.local.entity.BlockedProfileSessionEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }
    
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotBlank() }
    }
    
    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }
    
    @TypeConverter
    fun fromIntList(value: String?): List<Int>? {
        return value?.split(",")?.mapNotNull { it.toIntOrNull() }
    }
    
    @TypeConverter
    fun toIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }
    
    @TypeConverter
    fun fromPausedDurationList(value: String?): List<BlockedProfileSessionEntity.PausedDuration>? {
        if (value.isNullOrBlank()) return null
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            null
        }
    }
    
    @TypeConverter
    fun toPausedDurationList(list: List<BlockedProfileSessionEntity.PausedDuration>?): String? {
        if (list == null) return null
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            null
        }
    }
}
